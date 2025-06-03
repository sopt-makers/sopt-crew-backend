package org.sopt.makers.crew.main.external.auth.dto.response;

import java.util.List;

import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;

public record AuthUserResponseDto(
	Integer userId,
	String name,
	String profileImage,
	String birthday,
	String phone,
	String email,
	Integer lastGeneration,
	List<AuthUserActivityResponseDto> soptActivities
) {

	public User toEntity() {
		List<UserActivityVO> userActivityVOs = soptActivities.stream()
			.map(a -> new UserActivityVO(a.part(), a.generation()))
			.toList();

		return User.builder()
			// 여기서 id를 직접 넣어줘야 함(autoincrement 해제 후)
			.name(name)
			.profileImage(profileImage)
			.phone(phone)
			.activities(userActivityVOs)
			.build();
	}
}
