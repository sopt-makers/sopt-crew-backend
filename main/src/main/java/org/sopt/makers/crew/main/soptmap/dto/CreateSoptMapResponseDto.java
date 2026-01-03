package org.sopt.makers.crew.main.soptmap.dto;

public record CreateSoptMapResponseDto(Long soptMapId, Boolean firstRegistered) {

	public static CreateSoptMapResponseDto of(Long soptMapId, Boolean firstRegistered) {
		return new CreateSoptMapResponseDto(soptMapId, firstRegistered);
	}
}
