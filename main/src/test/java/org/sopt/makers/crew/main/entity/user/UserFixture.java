package org.sopt.makers.crew.main.entity.user;

import java.util.ArrayList;
import java.util.List;

import org.sopt.makers.crew.main.entity.user.enums.UserPart;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;

public class UserFixture {

	public static User createStaticUser() {
		List<UserActivityVO> activityVOS = new ArrayList<>();
		activityVOS.add(new UserActivityVO(UserPart.SERVER.getValue(), 33));
		activityVOS.add(new UserActivityVO(UserPart.SERVER.getValue(), 34));

		return User.builder()
			.name("송민규")
			.orgId(1)
			.activities(activityVOS)
			.phone("010-9472-6796")
			.build();
	}

	public static User createUser(Integer userId, String part, int generation) {
		return createUser(userId, List.of(new UserActivityVO(part, generation)));
	}

	public static User createUser(Integer userId, List<UserActivityVO> activities) {
		User user = User.builder()
			.name("테스트 유저")
			.orgId(userId)
			.activities(activities)
			.profileImage("https://example.com/profile.png")
			.phone("010-1234-5678")
			.build();
		user.setUserIdForTest(userId);
		return user;
	}
}
