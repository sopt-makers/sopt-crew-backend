package org.sopt.makers.crew.main.meeting.v2.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyType;

@Getter
public class ApplyInfoDto {
    private final Integer applyId;
    private final int type;
    private final String content;
    private final LocalDateTime appliedDate;
    private final int status;
    private final ApplicantDto applicant;

    @QueryProjection
    public ApplyInfoDto(Integer applyId, EnApplyType type, String content, LocalDateTime appliedDate, EnApplyStatus status,
                        ApplicantDto applicant) {
        this.applyId = applyId;
        this.type = type.getValue();
        this.content = content;
        this.appliedDate = appliedDate;
        this.status = status.getValue();
        this.applicant = applicant;
    }
}