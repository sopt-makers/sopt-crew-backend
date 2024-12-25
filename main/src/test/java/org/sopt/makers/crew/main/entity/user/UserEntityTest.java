package org.sopt.makers.crew.main.entity.user;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;

class UserEntityTest {

    @Test
    void 최근_기수_조회(){
        // given
        User user = User.builder()
                .name("홍길동")
                .orgId(1)
                .activities(List.of(new UserActivityVO("서버", 33), new UserActivityVO("iOS", 34)))
                .profileImage("image-url")
                .phone("010-1234-5678")
                .build();

        // when
        UserActivityVO recentActivityVO = user.getRecentActivityVO();

        // then

        Assertions.assertThat(recentActivityVO.getGeneration()).isEqualTo(34);
        Assertions.assertThat(recentActivityVO.getPart()).isEqualTo("iOS");

    }
}
