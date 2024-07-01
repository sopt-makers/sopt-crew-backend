package org.sopt.makers.crew.main.meeting.v2.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;

@Getter
@RequiredArgsConstructor
public class SimpleApplyInfoResponseDto {
    private final String name;
    private final EnApplyStatus status;
    private final String profileImage;

    public static SimpleApplyInfoResponseDto of(String name, EnApplyStatus status, String profileImage) {
        return new SimpleApplyInfoResponseDto(name, status, profileImage);
    }
}
