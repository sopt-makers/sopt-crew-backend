package org.sopt.makers.crew.main.entity.meeting;

import java.util.List;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetCreatedMeetingByUserQueryDto;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingQueryRepository {

  List<MeetingV2GetCreatedMeetingByUserQueryDto> findCreatedMeetingByUser(Integer userId);

}
