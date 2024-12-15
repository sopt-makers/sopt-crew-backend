package org.sopt.makers.crew.main.entity.meeting.vo;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class ImageUrlVO {
	private final Integer id;
	private final String url;

	@JsonCreator
	public ImageUrlVO(
		@JsonProperty("id") Integer id,
		@JsonProperty("url") String url) {

		validateImageUrlVO(id, url);
		this.id = id;
		this.url = url;
	}

	private void validateImageUrlVO(Integer id, String url) {
		if (id == null || id < 0) {
			throw new IllegalArgumentException(INVALID_INPUT_VALUE.getErrorCode()); // 커스텀 에러로 처리
		}
		if (url == null || url.isBlank()) {
			throw new IllegalArgumentException(INVALID_INPUT_VALUE.getErrorCode());  // 커스텀 에러로 처리
		}
	}
}
