package org.sopt.makers.crew.main.meeting.v2.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.sopt.makers.crew.main.common.pagination.dto.PageMetaDto;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "MeetingGetApplyListResponseDto", description = "모임 신청 목록 응답 Dto")
public class MeetingGetApplyListResponseDto {

    @Schema(description = "신청 목록", example = "")
    private final List<ApplyInfoDto> apply;

    private final PageMetaDto meta;
}
