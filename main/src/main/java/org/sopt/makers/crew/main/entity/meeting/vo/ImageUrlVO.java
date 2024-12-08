package org.sopt.makers.crew.main.entity.meeting.vo;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageUrlVO {
	private Integer id;
	private String url;

	public ImageUrlVO(Integer id, String url) {
		this.id = id;
		this.url = url;
	}
}
