package org.sopt.makers.crew.main.entity.apply;

import java.util.List;
import org.sopt.makers.crew.main.apply.v2.dto.query.ApplyV2GetAppliedMeetingByUserQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingGetAppliesQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.ApplyInfoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ApplySearchRepository {

  Page<ApplyInfoDto> findApplyList(MeetingGetAppliesQueryDto queryCommand, Pageable pageable,
      Integer meetingId,
      Integer meetingCreatorId, Integer userId);

  List<ApplyV2GetAppliedMeetingByUserQueryDto> findAppliedMeetingByUser(Integer userId);
}
