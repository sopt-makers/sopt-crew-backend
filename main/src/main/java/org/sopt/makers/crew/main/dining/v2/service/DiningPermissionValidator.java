package org.sopt.makers.crew.main.dining.v2.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import org.sopt.makers.crew.main.entity.dining.DiningAttendanceRepository;
import org.sopt.makers.crew.main.entity.dining.enums.DiningPayerStatus;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.global.exception.ForbiddenException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 회식 권한 검증
 * - 개설자 전용: 회식·회차 수정/삭제, 회차 추가, 마감, 종료, 결제자 승인·거절
 * - 개설자 + 승인된 공동 결제자: 참석자 관리(조회, 추가, 삭제, CSV)
 */
@Component
@RequiredArgsConstructor
public class DiningPermissionValidator {

	private final DiningAttendanceRepository diningAttendanceRepository;

	public void validateLeader(Meeting meeting, Integer userId) {
		meeting.validateMeetingCreator(userId);
	}

	public void validateLeaderOrApprovedPayer(Meeting meeting, Integer userId) {
		if (!isLeaderOrApprovedPayer(meeting, userId)) {
			throw new ForbiddenException(FORBIDDEN_EXCEPTION.getErrorCode());
		}
	}

	public boolean isLeader(Meeting meeting, Integer userId) {
		return meeting.checkMeetingLeader(userId);
	}

	/**
	 * 회식 단위 권한. 해당 회식의 회차 중 하나라도 승인된 결제자면 true
	 */
	public boolean isApprovedPayer(Integer meetingId, Integer userId) {
		return diningAttendanceRepository.existsByMeetingIdAndUserIdAndPayerStatus(meetingId, userId,
			DiningPayerStatus.APPROVED);
	}

	public boolean isLeaderOrApprovedPayer(Meeting meeting, Integer userId) {
		return isLeader(meeting, userId) || isApprovedPayer(meeting.getId(), userId);
	}
}
