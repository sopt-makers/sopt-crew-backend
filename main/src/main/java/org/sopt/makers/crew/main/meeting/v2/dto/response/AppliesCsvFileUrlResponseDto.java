package org.sopt.makers.crew.main.meeting.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Schema(name = "AppliesCsvFileUrlResponseDto", description = "csv url Dto")
public class AppliesCsvFileUrlResponseDto {

	@NotNull
	@Schema(description = "csv 파일 url", example = "[url] 형식")
	private final String url;

	public static AppliesCsvFileUrlResponseDto of(String url){
		return new AppliesCsvFileUrlResponseDto(url);
	}
}
