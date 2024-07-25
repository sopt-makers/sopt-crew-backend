package org.sopt.makers.crew.main.user.v2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class UserV2GetAllMentionUserDto {
    /**
     * 주의!! : 필드명은 userId 이지만 실제 응답 해야하는 데이터는 orgId 입니다.
     */
    private final Integer userId;

    private final String userName;
    private final String recentPart;
    private final int recentGeneration;
    private final String profileImageUrl;
}

