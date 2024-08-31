package org.sopt.makers.crew.main.external.s3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@RequiredArgsConstructor
public class S3Config {

	private final AwsProperties awsProperties;

	@Bean
	public S3Client s3Client() {
		AwsBasicCredentials awsBasicCredentials = getAwsBasicCredentials();

		return S3Client.builder()
			.region(Region.of(awsProperties.getAwsRegion()))
			.credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
			.build();
	}

	@Bean
	public S3Presigner s3Presigner() {
		AwsBasicCredentials awsBasicCredentials = getAwsBasicCredentials();

		return S3Presigner.builder()
			.region(Region.of(awsProperties.getAwsRegion()))
			.credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
			.build();
	}

	private AwsBasicCredentials getAwsBasicCredentials() {
		return AwsBasicCredentials.create(
			awsProperties.getAccessKey(),
			awsProperties.getSecretKey());
	}
}
