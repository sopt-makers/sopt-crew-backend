package org.sopt.makers.crew.main.meeting.v2.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;

@Getter
@Schema(name = "ApplyInfoDto", description = "모임 신청 객체 Dto")
public class ApplyInfoDto {

    @Schema(description = "신청 id", example = "1")
    private final Integer id;

    @Schema(description = "전하는 말", example = "저 뽑아주세요.")
    private final String content;

    @Schema(description = "신청 시간", example = "2024-07-30T15:30:00")
    private final LocalDateTime appliedDate;

    @Schema(description = "신청 상태", example = "1")
    private final EnApplyStatus status;

    @Schema(description = "신청자 정보", example = "")
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
