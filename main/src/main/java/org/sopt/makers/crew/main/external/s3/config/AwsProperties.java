package org.sopt.makers.crew.main.external.s3.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties("aws-property")
public final class AwsProperties {
	private final String awsRegion;
	private final String s3BucketName;
	private final String accessKey;
	private final String secretKey;
	private final long fileMinSize;
	private final long fileMaxSize;
	private final String algorithm;
	private final String contentType;
	private final String requestType;
}
