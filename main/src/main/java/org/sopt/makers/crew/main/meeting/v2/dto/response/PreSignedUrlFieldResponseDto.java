package org.sopt.makers.crew.main.meeting.v2.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PreSignedUrlFieldResponseDto {

	@JsonProperty("Content-Type")
	private final String contentType;

	@JsonProperty("key")
	private final String key;

	@JsonProperty("bucket")
	private final String bucket;

	@JsonProperty("X-Amz-Algorithm")
	private final String algorithm;

	@JsonProperty("X-Amz-Credential")
	private final String credential;

	@JsonProperty("X-Amz-Date")
	private final String date;

	@JsonProperty("Policy")
	private final String policy;

	@JsonProperty("X-Amz-Signature")
	private final String signature;

	public static PreSignedUrlFieldResponseDto of(String contentType, String key, String bucket, String algorithm,
		String credential,
		String date, String policy, String signature) {

		return new PreSignedUrlFieldResponseDto(contentType, key, bucket, algorithm, credential, date, policy,
			signature);
	}
}
