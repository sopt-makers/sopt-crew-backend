package org.sopt.makers.crew.main.external.s3.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.external.s3.config.AwsProperties;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

	@Mock
	private S3Client s3Client;
	@Mock
	private AwsProperties awsProperties;
	@TempDir
	Path tempDirectory;

	private S3Service s3Service;

	@BeforeEach
	void setUp() {
		s3Service = new S3Service(s3Client, awsProperties);
		given(awsProperties.getS3BucketName()).willReturn("test-bucket");
		given(awsProperties.getObjectUrl()).willReturn("https://image.test/");
	}

	@ParameterizedTest(name = "절대경로 여부: {0}")
	@ValueSource(booleans = {false, true})
	void uploadCSVFile_usesOnlyFileNameInObjectKeyAndUploadsOriginalBytes(boolean absolutePath) throws Exception {
		// 기존 일반 모임의 파일명 입력과 임시 디렉터리 절대경로 입력을 모두 검증한다.
		Path file = Files.createTempFile(absolutePath ? tempDirectory : Path.of("."), "csv-test-", ".csv");
		try {
			byte[] content = "\uFEFF번호,이름,최근 기수,연락처\n".getBytes(java.nio.charset.StandardCharsets.UTF_8);
			Files.write(file, content);
			String filePath = absolutePath ? file.toAbsolutePath().toString() : file.getFileName().toString();

			String url = s3Service.uploadCSVFile(filePath);

			var requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
			var bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
			verify(s3Client).putObject(requestCaptor.capture(), bodyCaptor.capture());
			var request = requestCaptor.getValue();
			assertThat(request.bucket()).isEqualTo("test-bucket");
			assertThat(request.key()).matches("meeting/\\d{4}/\\d{2}/\\d{2}/" + file.getFileName());
			assertThat(url).isEqualTo("https://image.test/" + request.key());
			try (var input = bodyCaptor.getValue().contentStreamProvider().newStream()) {
				assertThat(input.readAllBytes()).isEqualTo(content);
			}
		} finally {
			Files.deleteIfExists(file);
		}
	}
}
