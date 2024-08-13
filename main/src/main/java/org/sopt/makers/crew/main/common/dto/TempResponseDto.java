package org.sopt.makers.crew.main.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(name = "TempResponseDto", description = "임시 응답 Dto")
public class TempResponseDto<T> {

	@Schema(description = "임시 응답", example = "")
	@NotNull
	private T data;

	public static <T> TempResponseDto<T> of(T data) {
		return new TempResponseDto<T>(data);
	}
}
