package org.sopt.makers.crew.main.meeting.v2.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PreSignedUrlFieldResponseDto {

	@JsonProperty("Content-Type")
	@NotNull
	private final String contentType;

	@JsonProperty("key")
	@NotNull
	private final String key;

	@JsonProperty("bucket")
	@NotNull
	private final String bucket;

	@JsonProperty("X-Amz-Algorithm")
	@NotNull
	private final String algorithm;

	@JsonProperty("X-Amz-Credential")
	@NotNull
	private final String credential;

	@JsonProperty("X-Amz-Date")
	@NotNull
	private final String date;

	@JsonProperty("Policy")
	@NotNull
	private final String policy;

	@JsonProperty("X-Amz-Signature")
	@NotNull
	private final String signature;

	public static PreSignedUrlFieldResponseDto of(String contentType, String key, String bucket, String algorithm,
		String credential,
		String date, String policy, String signature) {

		return new PreSignedUrlFieldResponseDto(contentType, key, bucket, algorithm, credential, date, policy,
			signature);
	}
}
