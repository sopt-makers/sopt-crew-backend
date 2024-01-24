package org.sopt.makers.crew.main.meeting.v2.dto.query;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.sopt.makers.crew.main.common.pagination.dto.PageOptionsDto;

@Getter
public class MeetingV2GetAllMeetingByOrgUserQueryDto extends PageOptionsDto {

  @NotNull
  private Integer orgUserId;

  public MeetingV2GetAllMeetingByOrgUserQueryDto(Integer orgUserId, Integer page, Integer take) {
    super(page, take);
    this.orgUserId = orgUserId;
  }
}
