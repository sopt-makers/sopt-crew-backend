package org.sopt.makers.crew.main.user.v2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserV2GetAllMentionUserDto {
    private Integer userId;
    private String userName;
    private String recentPart;
    private int recentGeneration;
    private String profileImageUrl;

    public static UserV2GetAllMentionUserDto of(Integer userId, String userName, String recentPart, int recentGeneration, String profileImageUrl) {
        return new UserV2GetAllMentionUserDto(userId, userName, recentPart, recentGeneration, profileImageUrl);
    }
}

