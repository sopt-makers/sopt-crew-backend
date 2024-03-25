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
}
