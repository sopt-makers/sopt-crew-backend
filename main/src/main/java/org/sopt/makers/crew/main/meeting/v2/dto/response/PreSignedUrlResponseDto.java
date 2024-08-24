package org.sopt.makers.crew.main.meeting.v2.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PreSignedUrlResponseDto {
	private final String url;
	private final PreSignedUrlFieldResponseDto fields;

	public static PreSignedUrlResponseDto of(String url, PreSignedUrlFieldResponseDto fields){

		return new PreSignedUrlResponseDto(url, fields);
	}
}
