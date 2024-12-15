package org.sopt.makers.crew.main.entity.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.global.exception.ServerException;

class UserActivityVOTest {
	@Test
	void 최근_기수_조회_잘못된_데이터가_저장된_경우_예외가_발생한다(){

		// given, when, then
		Assertions.assertThatThrownBy(() -> new UserActivityVO(null, 34))
			.isInstanceOf(ServerException.class);
	}
}
