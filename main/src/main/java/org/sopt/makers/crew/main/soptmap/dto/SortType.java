package org.sopt.makers.crew.main.soptmap.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SortType {

	LATEST("최신순"),
	POPULAR("추천순");

	private final String description;
}
