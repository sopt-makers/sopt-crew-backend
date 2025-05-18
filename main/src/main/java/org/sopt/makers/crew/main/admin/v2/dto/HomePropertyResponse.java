package org.sopt.makers.crew.main.admin.v2.dto;

import java.util.List;

import org.sopt.makers.crew.main.admin.v2.service.vo.MainPageContentVo;

public record HomePropertyResponse(List<MainPageContentVo> home) {

	public static HomePropertyResponse from(List<MainPageContentVo> home) {
		return new HomePropertyResponse(home);
	}

}
