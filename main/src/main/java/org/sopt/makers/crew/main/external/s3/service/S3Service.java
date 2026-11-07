package org.sopt.makers.crew.main.external.s3.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.sopt.makers.crew.main.external.s3.config.AwsProperties;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.ServerException;
import org.sopt.makers.crew.main.meeting.v2.dto.response.PreSignedUrlFieldResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.PreSignedUrlResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.utils.BinaryUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {
	private static final String MEETING_PATH = "meeting";
	private static final String MEETING_TOP_PATH = "meeting_top";
	private static final String S3_SERVCIE = "s3";
	private static final String PRE_SIGNED_URL_PREFIX = "https://s3.ap-northeast-2.amazonaws.com";
	private static final String IMAGE_MIME_TYPE_PREFIX = "image/";
	/**
	 * Pre-Signed URL 로 업로드 가능한 이미지 확장자 (FE 모임/회식 작성 정책과 동일: JPG/JPEG, PNG, WebP, GIF)
	 */
	private static final Set<String> ALLOWED_PRE_SIGNED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

	private final S3Client s3Client;

	private final AwsProperties awsProperties;

	public String uploadCSVFile(String fileName) {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
			String curDate = LocalDateTime.now().format(formatter);

			File file = new File(fileName);
			String objectKey = MEETING_PATH + "/" + curDate + "/" + file.getName();
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(awsProperties.getS3BucketName())
				.key(objectKey)
				.build();

			s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
			log.info("File uploaded successfully to S3: {}", fileName);

			return awsProperties.getObjectUrl() + objectKey;
		} catch (Exception e) {
			throw new ServerException(S3_STORAGE_ERROR.getErrorCode());
		}

	}

	public String uploadMeetingTopImage(MultipartFile imageFile) {
		validateImageFile(imageFile);

		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
			String curDate = LocalDateTime.now().format(formatter);
			String objectKey = MEETING_TOP_PATH + "/" + curDate + "/" + UUID.randomUUID() + "."
				+ getImageExtension(imageFile.getContentType());

			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(awsProperties.getS3BucketName())
				.key(objectKey)
				.contentType(imageFile.getContentType())
				.build();

			s3Client.putObject(putObjectRequest,
				RequestBody.fromInputStream(imageFile.getInputStream(), imageFile.getSize()));
			log.info("Meeting top image uploaded successfully to S3: {}", objectKey);

			return createPublicUrl(objectKey);
		} catch (Exception e) {
			throw new ServerException(S3_STORAGE_ERROR.getErrorCode());
		}
	}

	public PreSignedUrlResponseDto generatePreSignedUrl(String contentType) {
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
		dateTimeFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));
		SimpleDateFormat dateStampFormat = new SimpleDateFormat("yyyyMMdd");
		dateStampFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));

		Date now = new Date();
		String dateTimeStamp = dateTimeFormat.format(now);
		String dateStamp = dateStampFormat.format(now);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(now);
		calendar.add(Calendar.MINUTE, 10);
		Date expirationDate = calendar.getTime();

		String extension = toAllowedPreSignedImageExtension(contentType);
		String objectKey = getObjectKey(extension);
		String encodedPolicy = generateEncodedPolicy(awsProperties.getS3BucketName(), objectKey, dateTimeStamp,
			dateStamp, expirationDate);

		String signature = sign(encodedPolicy, dateStamp, S3_SERVCIE);
		String credentials = getCredentials(dateStamp);

		PreSignedUrlFieldResponseDto fieldResponseDto = new PreSignedUrlFieldResponseDto(awsProperties.getContentType(),
			objectKey, awsProperties.getS3BucketName(), awsProperties.getAlgorithm(), credentials, dateTimeStamp,
			encodedPolicy,
			signature);

		return PreSignedUrlResponseDto.of(PRE_SIGNED_URL_PREFIX + "/" + awsProperties.getS3BucketName(),
			fieldResponseDto);
	}

	/**
	 * Pre-Signed URL 발급 요청의 contentType 을 허용된 이미지 확장자로 변환한다.
	 * 확장자(jpeg)와 MIME Type(image/jpeg) 형식을 모두 받으며, 허용 목록에 없으면 거부한다.
	 * MIME 매개변수(image/jpeg; charset=UTF-8)는 제외하고, 앞뒤 공백은 Unicode 공백까지 제거한다.
	 */
	private String toAllowedPreSignedImageExtension(String contentType) {
		if (contentType == null || contentType.isBlank()) {
			throw new BadRequestException("이미지 파일의 Content-Type이 없습니다.");
		}

		String extension = removeMimeParameters(contentType).strip().toLowerCase(Locale.ROOT);
		if (extension.startsWith(IMAGE_MIME_TYPE_PREFIX)) {
			extension = extension.substring(IMAGE_MIME_TYPE_PREFIX.length()).strip();
		}

		if (!ALLOWED_PRE_SIGNED_IMAGE_EXTENSIONS.contains(extension)) {
			throw new BadRequestException("JPG/JPEG, PNG, WebP, GIF 형식의 이미지만 업로드할 수 있습니다.");
		}
		return extension;
	}

	private String removeMimeParameters(String contentType) {
		int parameterIndex = contentType.indexOf(';');
		if (parameterIndex < 0) {
			return contentType;
		}
		return contentType.substring(0, parameterIndex);
	}

	/**
	 * 파일의 경로명을 랜덤으로 생성
	 * @author @mikekks
	 * @param extension 파일 확장자 (허용 목록 검증을 거친 값)
	 * @returns 파일의 경로명
	 */
	private String getObjectKey(String extension) {
		UUID uuid = UUID.randomUUID();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		String curDate = LocalDateTime.now().format(formatter);

		return MEETING_PATH + "/" + curDate + "/" + uuid + "." + extension;
	}

	private void validateImageFile(MultipartFile imageFile) {
		if (imageFile == null || imageFile.isEmpty()) {
			throw new BadRequestException("업로드할 이미지 파일이 없습니다.");
		}
		if (imageFile.getSize() < awsProperties.getFileMinSize()
			|| imageFile.getSize() > awsProperties.getFileMaxSize()) {
			throw new BadRequestException("이미지 파일 크기가 허용 범위를 벗어났습니다.");
		}

		getImageExtension(imageFile.getContentType());
	}

	private String getImageExtension(String contentType) {
		if (contentType == null) {
			throw new BadRequestException("이미지 파일의 Content-Type이 없습니다.");
		}

		return switch (contentType) {
			case "image/jpeg", "image/jpg" -> "jpg";
			case "image/png" -> "png";
			case "image/gif" -> "gif";
			case "image/webp" -> "webp";
			default -> throw new BadRequestException("이미지 파일만 업로드할 수 있습니다.");
		};
	}

	private String createPublicUrl(String objectKey) {
		if (awsProperties.getObjectUrl().endsWith("/")) {
			return awsProperties.getObjectUrl() + objectKey;
		}
		return awsProperties.getObjectUrl() + "/" + objectKey;
	}

	/**
	 * PreSignedUrl 업로드 정책 생성
	 * @author @mikekks
	 * @returns base64로 인코딩된 String
	 */
	public String generateEncodedPolicy(String bucketName, String objectKey, String dateTimeStamp, String dateStamp,
		Date expirationDate) {
		List<?> conditions = generateConditions(bucketName, objectKey, dateTimeStamp,
			dateStamp);

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		simpleDateFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));

		Map<String, Object> policy = Map.of(
			"expiration", simpleDateFormat.format(expirationDate),
			"conditions", conditions
		);

		return encodePolicy(policy);
	}

	private List<?> generateConditions(String bucketName, String objectKey, String dateTimeStamp, String dateStamp) {
		List<?> contentLengthRange = List.of("content-length-range", awsProperties.getFileMinSize(),
			awsProperties.getFileMaxSize());

		List<Object> conditions = new LinkedList<>();
		conditions.add(contentLengthRange);

		Map<String, String> params = new HashMap<>();
		String credentials = getCredentials(dateStamp);
		params.put("Content-Type", awsProperties.getContentType());
		params.put("key", objectKey);
		params.put("bucket", bucketName);
		params.put("X-Amz-Algorithm", awsProperties.getAlgorithm());
		params.put("X-Amz-Credential", credentials);
		params.put("X-Amz-Date", dateTimeStamp);

		Map<String, String> awsDefaults = generateAwsDefaultParams(bucketName, objectKey, dateTimeStamp, dateStamp);

		conditions.addAll(awsDefaults.entrySet());

		return conditions;
	}

	private Map<String, String> generateAwsDefaultParams(String bucketName, String objectKey, String dateTimeStamp,
		String dateStamp) {

		String credentials = getCredentials(dateStamp);
		Map<String, String> params = new HashMap<>();
		params.put("Content-Type", awsProperties.getContentType());
		params.put("key", objectKey);
		params.put("bucket", bucketName);
		params.put("X-Amz-Algorithm", awsProperties.getAlgorithm());
		params.put("X-Amz-Credential", credentials);
		params.put("X-Amz-Date", dateTimeStamp);

		return params;
	}

	private String getCredentials(String dateStamp) {
		return String.format("%s/%s/%s/%s/%s", awsProperties.getAccessKey(), dateStamp, awsProperties.getAwsRegion(),
			S3_SERVCIE,
			awsProperties.getRequestType());
	}

	private String encodePolicy(Map<String, Object> policy) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonPolicy = objectMapper.writeValueAsString(policy);

			return Base64.getEncoder().encodeToString(jsonPolicy.getBytes());
		} catch (Exception e) {
			throw new ServerException(S3_STORAGE_ERROR.getErrorCode());
		}
	}

	public String sign(String toSign, String dateStamp, String service) {
		try {
			String awsSecretKey = awsProperties.getSecretKey();

			byte[] kSecret = ("AWS4" + awsSecretKey).getBytes(StandardCharsets.UTF_8);
			byte[] kDate = hmacSHA256(dateStamp, kSecret);
			byte[] kRegion = hmacSHA256("ap-northeast-2", kDate);
			byte[] kService = hmacSHA256(service, kRegion);
			byte[] kSigning = hmacSHA256("aws4_request", kService);
			byte[] signature = hmacSHA256(toSign, kSigning);

			return BinaryUtils.toHex(signature);
		} catch (Exception e) {
			throw new ServerException(S3_STORAGE_ERROR.getErrorCode());
		}
	}

	private byte[] hmacSHA256(String data, byte[] key) {
		try {
			String algorithm = "HmacSHA256";
			Mac mac = Mac.getInstance(algorithm);
			mac.init(new SecretKeySpec(key, algorithm));
			return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new ServerException(S3_STORAGE_ERROR.getErrorCode());
		}
	}

}
