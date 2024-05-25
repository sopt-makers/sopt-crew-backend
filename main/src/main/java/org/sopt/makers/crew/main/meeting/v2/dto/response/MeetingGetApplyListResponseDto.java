package org.sopt.makers.crew.main.meeting.v2.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.sopt.makers.crew.main.common.pagination.dto.PageMetaDto;

@Getter
@AllArgsConstructor(staticName = "of")
public class MeetingGetApplyListResponseDto {

    private final List<ApplyInfoDto> applies;
    private final PageMetaDto meta;
}
