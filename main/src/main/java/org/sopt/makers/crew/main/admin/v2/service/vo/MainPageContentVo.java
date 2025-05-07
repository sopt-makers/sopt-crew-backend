package org.sopt.makers.crew.main.admin.v2.service.vo;

import java.util.List;

public record MainPageContentVo(String title, List<Integer> meetingIds) {

	public static MainPageContentVo of(String title, List<Integer> meetingIds) {
		return new MainPageContentVo(title, meetingIds);
	}

}
