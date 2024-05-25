package org.sopt.makers.crew.main.meeting.v2.dto.query;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyType;

@Getter
public class MeetingGetApplyListCommand {

    private List<EnApplyStatus> statuses;
    private List<EnApplyType> types;
    private String date;

    @Builder
    public MeetingGetApplyListCommand(List<Integer> statuses, List<Integer> types,
                                      String date) {
        this.statuses = statuses.stream().map(EnApplyStatus::ofValue).toList();
        this.types = types.stream().map(EnApplyType::ofValue).toList();
        this.date = date;
    }
}
