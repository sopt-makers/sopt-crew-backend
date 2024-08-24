package org.sopt.makers.crew.main.meeting.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Schema(name = "PreSignedUrlResponseDto", description = "presigned url Dto")
public class PreSignedUrlResponseDto {

	@NotNull
	@Schema(description = "presignedUrl", example = "[url] 형식")
	private final String url;

	@NotNull
	@Schema(description = "field", example = "")
	private final PreSignedUrlFieldResponseDto fields;

	public static PreSignedUrlResponseDto of(String url, PreSignedUrlFieldResponseDto fields){

		return new PreSignedUrlResponseDto(url, fields);
	}
}
