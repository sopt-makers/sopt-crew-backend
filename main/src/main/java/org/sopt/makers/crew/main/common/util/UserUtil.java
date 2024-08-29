package org.sopt.makers.crew.main.common.util;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.sopt.makers.crew.main.common.exception.UnAuthorizedException;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;

@RequiredArgsConstructor
public class UserUtil {

	public static Integer getUserId(Principal principal) {
		if (principal == null) {
			throw new UnAuthorizedException();
		}
		return Integer.valueOf(principal.getName());
	}

	public static UserActivityVO getRecentUserActivity(List<UserActivityVO> userActivityVOs) {
		return userActivityVOs.stream().max(Comparator.comparingInt(UserActivityVO::getGeneration)).orElse(null);
	}
}
