package org.sopt.makers.crew.main.meeting.v2.dto.query;

import java.util.Arrays;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.sopt.makers.crew.main.common.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;

@Getter
@Setter
public class MeetingGetApplyListCommand extends PageOptionsDto {

    private List<EnApplyStatus> status;
    private String date;

    @Builder
    public MeetingGetApplyListCommand(int page, int take, List<EnApplyStatus> status, String date) {
        super(page, take);
        this.status = (status == null || status.isEmpty()) ?
                Arrays.asList(EnApplyStatus.WAITING, EnApplyStatus.APPROVE, EnApplyStatus.REJECT) : status;
        this.date = date == null ? "desc" : date;
    }
}
