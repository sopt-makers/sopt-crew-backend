package org.sopt.makers.crew.main.auth.v2.service;

import org.sopt.makers.crew.main.auth.v2.dto.response.AuthV2ResponseDto;

public interface AuthV2Service {
	AuthV2ResponseDto loginUser(Integer userId);
}
