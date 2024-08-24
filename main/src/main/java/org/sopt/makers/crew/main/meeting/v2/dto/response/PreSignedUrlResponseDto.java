package org.sopt.makers.crew.main.meeting.v2.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PreSignedUrlResponseDto {

	@NotNull
	private final String url;

	@NotNull
	private final PreSignedUrlFieldResponseDto fields;

	public static PreSignedUrlResponseDto of(String url, PreSignedUrlFieldResponseDto fields){

		return new PreSignedUrlResponseDto(url, fields);
	}
}
