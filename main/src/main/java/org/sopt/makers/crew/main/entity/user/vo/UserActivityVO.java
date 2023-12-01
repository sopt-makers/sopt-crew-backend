package org.sopt.makers.crew.main.entity.user.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.entity.user.enums.UserPart;

@Getter
@RequiredArgsConstructor
public class UserActivityVO {
    private final int generation;
    private final UserPart part;
}
