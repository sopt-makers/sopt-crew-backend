package org.sopt.makers.crew.main.entity.apply;

import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingGetApplyListCommand;
import org.sopt.makers.crew.main.meeting.v2.dto.response.ApplyInfoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ApplySearchRepository {
    Page<ApplyInfoDto> findApplyList(MeetingGetApplyListCommand queryCommand, Pageable pageable, Integer meetingId,
                                     Integer studyCreatorId, Integer userId);
}
