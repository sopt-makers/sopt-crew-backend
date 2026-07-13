package org.sopt.makers.crew.main.post.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class MumuTextResponseDto {

	@Schema(description = "무무 텍스트", example = "38기동안 즐겁게 스터디를 즐겼음메? ~~~")
	private String mumuText;

	public static MumuTextResponseDto from(String mumuText) {
		return MumuTextResponseDto.builder().mumuText(mumuText).build();
	}

}
