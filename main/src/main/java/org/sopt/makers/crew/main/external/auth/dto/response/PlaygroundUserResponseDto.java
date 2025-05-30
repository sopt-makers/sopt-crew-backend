package org.sopt.makers.crew.main.external.auth.dto.response;

import java.util.List;

import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @note: id는 orgId 의미
 *
 * */
@RequiredArgsConstructor
@Getter
public class PlaygroundUserResponseDto {

	private final Integer id;
	private final String name;
	private final String profileImage;
	private final String phone;
	private final List<PlaygroundUserActivityResponseDto> activities;

	public User toEntity() {
		List<UserActivityVO> userActivityVOs = activities.stream()
			.map(a -> new UserActivityVO(a.part(), a.generation()))
			.toList();

		return User.builder()
			.orgId(this.id)
			.name(name)
			.profileImage(profileImage)
			.phone(phone)
			.activities(userActivityVOs)
			.build();
	}

	public List<UserActivityVO> getUserActivities() {
		return activities.stream()
			.map(a -> new UserActivityVO(a.part(), a.generation()))
			.toList();
	}
}
