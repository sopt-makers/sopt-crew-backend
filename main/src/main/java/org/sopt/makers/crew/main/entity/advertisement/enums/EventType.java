package org.sopt.makers.crew.main.entity.advertisement.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
	// 솝커톤
	SOPKATHON("솝커톤"),

	// TODO: 네트워킹도 솝커톤과 동일한 플로우로 구현 예정
	NETWORKING("네트워킹");

	private final String displayName;
}
