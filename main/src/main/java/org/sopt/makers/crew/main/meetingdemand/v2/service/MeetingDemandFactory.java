package org.sopt.makers.crew.main.meetingdemand.v2.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.INVALID_MEETING_KEYWORD_SIZE;

import java.util.List;

import org.sopt.makers.crew.main.entity.meeting.vo.MeetingJoinInfo;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemand;
import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.request.MeetingDemandV2CreateMeetingDemandBodyDto;
import org.springframework.stereotype.Component;

@Component
public class MeetingDemandFactory {

	private static final int MAX_MEETING_KEYWORD_SIZE = 2;

	public MeetingDemand create(User user, MeetingDemandV2CreateMeetingDemandBodyDto requestBody) {
		return MeetingDemand.builder()
			.user(user)
			.shortIntro(requestBody.getShortIntro())
			.expectation(requestBody.getExpectation())
			.meetingKeywordTypes(toMeetingKeywordTypes(requestBody.getMeetingKeywordTypes()))
			.joinInfo(toMeetingJoinInfo(requestBody.getJoinInfo()))
			.build();
	}

	private MeetingJoinInfo toMeetingJoinInfo(MeetingJoinInfo joinInfo) {
		if (joinInfo == null || joinInfo.isEmpty()) {
			return null;
		}

		return joinInfo;
	}

	private List<MeetingKeywordType> toMeetingKeywordTypes(List<String> values) {
		if (values == null || values.isEmpty() || values.size() > MAX_MEETING_KEYWORD_SIZE) {
			throw new BadRequestException(INVALID_MEETING_KEYWORD_SIZE.getErrorCode());
		}

		return values.stream()
			.map(MeetingKeywordType::ofValue)
			.toList();
	}
}
