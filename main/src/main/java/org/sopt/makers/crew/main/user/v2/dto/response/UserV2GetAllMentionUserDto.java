package org.sopt.makers.crew.main.user.v2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class UserV2GetAllMentionUserDto {
    private Integer userId;
    private String userName;
    private String part;
    private int recentGeneration;
    private String profileImageUrl;
}

