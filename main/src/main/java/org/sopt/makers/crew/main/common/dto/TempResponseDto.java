package org.sopt.makers.crew.main.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TempResponseDto<T> {
	private T data;

	public static <T> TempResponseDto<T> of(T data) {
		return new TempResponseDto<T>(data);
	}
}
