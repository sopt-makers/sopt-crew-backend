package org.sopt.makers.crew.main.external.s3.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.sopt.makers.crew.main.external.s3.config.AwsProperties;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.meeting.v2.dto.response.PreSignedUrlResponseDto;

import software.amazon.awssdk.services.s3.S3Client;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class S3ServicePreSignedUrlTest {

	@Mock
	private S3Client s3Client;
	@Mock
	private AwsProperties awsProperties;

	private S3Service s3Service;

	@BeforeEach
	void setUp() {
		s3Service = new S3Service(s3Client, awsProperties);
		given(awsProperties.getS3BucketName()).willReturn("test-bucket");
		given(awsProperties.getContentType()).willReturn("image/");
		given(awsProperties.getAlgorithm()).willReturn("AWS4-HMAC-SHA256");
		given(awsProperties.getAccessKey()).willReturn("access-key");
		given(awsProperties.getSecretKey()).willReturn("secret-key");
		given(awsProperties.getAwsRegion()).willReturn("ap-northeast-2");
		given(awsProperties.getRequestType()).willReturn("aws4_request");
		given(awsProperties.getFileMinSize()).willReturn(0L);
		given(awsProperties.getFileMaxSize()).willReturn(10L);
	}

	@ParameterizedTest(name = "{0} -> .{1}")
	@CsvSource({
		"jpg, jpg",
		"jpeg, jpeg",
		"png, png",
		"webp, webp",
		"gif, gif",
		"JPEG, jpeg",
		"image/jpeg, jpeg",
		"image/png, png",
		"IMAGE/WEBP, webp",
		"'image/jpeg; charset=UTF-8', jpeg",
		"'image/png;', png",
		"'\u2003jpeg\u2003', jpeg",
		"'\u3000image/gif\u2009', gif",
		"'image/ webp ; q=1', webp"
	})
	void generatePreSignedUrl_withAllowedType_usesExtensionInObjectKey(String contentType, String extension) {
		PreSignedUrlResponseDto response = s3Service.generatePreSignedUrl(contentType);

		assertThat(response.getFields().getKey()).matches("meeting/\\d{4}/\\d{2}/\\d{2}/[0-9a-f-]{36}\\." + extension);
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = {"svg", "image/svg+xml", "heic", "bmp", "pdf", "application/pdf", "html", "jpeg/../x",
		"png.html", "image/svg+xml; charset=UTF-8", "; jpeg", "application/jpeg"})
	void generatePreSignedUrl_withDisallowedType_throwsBadRequest(String contentType) {
		assertThatThrownBy(() -> s3Service.generatePreSignedUrl(contentType))
			.isInstanceOf(BadRequestException.class);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "\u2003"})
	void generatePreSignedUrl_withoutContentType_throwsBadRequest(String contentType) {
		assertThatThrownBy(() -> s3Service.generatePreSignedUrl(contentType))
			.isInstanceOf(BadRequestException.class);
	}
}
