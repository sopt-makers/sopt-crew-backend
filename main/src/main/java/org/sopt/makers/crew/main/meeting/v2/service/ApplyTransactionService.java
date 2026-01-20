package org.sopt.makers.crew.main.meeting.v2.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.FULL_MEETING_CAPACITY;

import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyType;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.meeting.v2.dto.ApplyMapper;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2ApplyMeetingDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Apply 저장을 위한 트랜잭션 전용 서비스
 * <p>
 * MeetingV2ServiceImpl에서 트랜잭션 범위를 축소하기 위해 분리됨.
 * Self-invocation 문제를 해결하고, 쓰기 작업만 트랜잭션 내에서 실행.
 */
@Service
@RequiredArgsConstructor
public class ApplyTransactionService {

	private final ApplyRepository applyRepository;
	private final ApplyMapper applyMapper;

	/**
	 * Apply 엔티티를 생성하고 저장
	 * <p>
	 * Sequential Transaction Pattern: 읽기 트랜잭션 완료 후 별도의 쓰기 트랜잭션에서 실행
	 * Race Condition 방지를 위해 쓰기 트랜잭션 내에서 capacity 재검증
	 *
	 * @param requestBody 신청 요청 DTO
	 * @param applyType   신청 유형
	 * @param meeting     모임 엔티티
	 * @param user        사용자 엔티티
	 * @param userId      사용자 ID
	 * @return 저장된 Apply 엔티티
	 */
	@Transactional
	public Apply saveApply(MeetingV2ApplyMeetingDto requestBody,
		EnApplyType applyType,
		Meeting meeting,
		User user,
		Integer userId) {
		// Race Condition 방지: 쓰기 트랜잭션 내에서 capacity 재검증
		int currentApplyCount = applyRepository.countByMeetingId(meeting.getId());
		if (currentApplyCount >= meeting.getCapacity()) {
			throw new BadRequestException(FULL_MEETING_CAPACITY.getErrorCode());
		}

		Apply apply = applyMapper.toApplyEntity(requestBody, applyType, meeting, user, userId);
		return applyRepository.save(apply);
	}
}
