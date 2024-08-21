package org.sopt.makers.crew.main.entity.meeting;

import org.sopt.makers.crew.main.common.util.Time;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MeetingSearchRepository {
	Page<Meeting> findAllByQuery(MeetingV2GetAllMeetingQueryDto queryCommand, Pageable pageable, Time time);
}
