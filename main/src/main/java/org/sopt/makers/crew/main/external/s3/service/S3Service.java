package org.sopt.makers.crew.main.external.s3.service;

import static org.sopt.makers.crew.main.common.exception.ErrorStatus.*;

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
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.sopt.makers.crew.main.common.exception.ServerException;
import org.sopt.makers.crew.main.external.s3.config.AwsProperties;
import org.sopt.makers.crew.main.meeting.v2.dto.response.PreSignedUrlFieldResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.PreSignedUrlResponseDto;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.utils.BinaryUtils;

@Service
@RequiredArgsConstructor
public class S3Service {
	private static final String MEETING_PATH = "meeting";
	private static final String S3_SERVCIE = "s3";
	private static final String PRE_SIGNED_URL_PREFIX = "https://s3.ap-northeast-2.amazonaws.com";

	private final S3Client s3Client;

	private final AwsProperties awsProperties;

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

		String objectKey = getObjectKey(contentType);
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
	 * 파일의 경로명을 랜덤으로 생성
	 * @author @mikekks
	 * @param contentType 파일의 컨텐츠 타입
	 * @returns 파일의 경로명
	 */
	private String getObjectKey(String contentType) {
		UUID uuid = UUID.randomUUID();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		String curDate = LocalDateTime.now().format(formatter);

		return MEETING_PATH + "/" + curDate + "/" + uuid + "." + contentType;
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
