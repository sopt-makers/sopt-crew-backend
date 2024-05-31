package org.sopt.makers.crew.main.meeting.v2.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;

@Getter
public class ApplyInfoDto {
    private final Integer id;
    private final String content;
    private final LocalDateTime appliedDate;
    private final EnApplyStatus status;
    private final ApplicantDto user;

    @QueryProjection
    public ApplyInfoDto(Integer id, String content, LocalDateTime appliedDate, EnApplyStatus status,
                        ApplicantDto user) {
        this.id = id;
        this.content = content;
        this.appliedDate = appliedDate;
        this.status = status;
        this.user = user;
    }
}
