package org.sopt.makers.crew.main.meeting.v2.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyType;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2ApplyMeetingDto;

@Mapper(componentModel = "spring")
public interface ApplyMapper {

    @Mapping(source = "requestBody.meetingId", target = "meetingId")
    @Mapping(source = "requestBody.content", target = "content")
    Apply toApplyEntity(MeetingV2ApplyMeetingDto requestBody, EnApplyType type, Meeting meeting,
                        User user,
                        Integer userId);
}
