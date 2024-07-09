package org.sopt.makers.crew.main.user.v2.service;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllMentionUserDto;

@Getter
@NoArgsConstructor
public class Users {
    private List<UserV2GetAllMentionUserDto> userDtos = new ArrayList<>();

    public Users(List<UserV2GetAllMentionUserDto> userDtos) {
        this.userDtos = userDtos;
    }
}
