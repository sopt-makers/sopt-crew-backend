package org.sopt.makers.crew.main.meeting.v2.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Schema(name = "PreSignedUrlFieldResponseDto", description = "presigned 필드 Dto")
public class PreSignedUrlFieldResponseDto {

	@JsonProperty("Content-Type")
	@NotNull
	@Schema(description = "contentType", example = "image/jpeg")
	private final String contentType;

	@JsonProperty("key")
	@NotNull
	@Schema(description = "key", example = "key 값")
	private final String key;

	@JsonProperty("bucket")
	@NotNull
	@Schema(description = "bucket", example = "bucket 값")
	private final String bucket;

	@JsonProperty("X-Amz-Algorithm")
	@NotNull
	@Schema(description = "algorithm", example = "algorithm 값")
	private final String algorithm;

	@JsonProperty("X-Amz-Credential")
	@NotNull
	@Schema(description = "credential", example = "credential 값")
	private final String credential;

	@JsonProperty("X-Amz-Date")
	@NotNull
	@Schema(description = "X-Amz-Date", example = "X-Amz-Date 값")
	private final String date;

	@JsonProperty("Policy")
	@NotNull
	@Schema(description = "policy", example = "policy 값")
	private final String policy;

	@JsonProperty("X-Amz-Signature")
	@NotNull
	@Schema(description = "X-Amz-Signature", example = "X-Amz-Signature 값")
	private final String signature;

	public static PreSignedUrlFieldResponseDto of(String contentType, String key, String bucket, String algorithm,
		String credential,
		String date, String policy, String signature) {

		return new PreSignedUrlFieldResponseDto(contentType, key, bucket, algorithm, credential, date, policy,
			signature);
	}
}
