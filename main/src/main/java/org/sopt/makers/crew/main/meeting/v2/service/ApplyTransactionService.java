package org.sopt.makers.crew.main.meeting.v2.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.FULL_MEETING_CAPACITY;

import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyType;
import org.sopt.makers.crew.main.entity.meeting.CoLeaderRepository;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.meeting.v2.dto.ApplyMapper;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2ApplyMeetingDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2ApplyMeetingResponseDto;
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
	private final MeetingRepository meetingRepository;
	private final UserRepository userRepository;
	private final CoLeaderRepository coLeaderRepository;
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
		// Race Condition 방지: 쓰기 트랜잭션 내에서 capacity 재검증 (APPROVE만 — develop 비즈니스 로직과 일치)
		int approvedCount = applyRepository.countByMeetingIdAndStatus(meeting.getId(), EnApplyStatus.APPROVE);
		if (approvedCount >= meeting.getCapacity()) {
			throw new BadRequestException(FULL_MEETING_CAPACITY.getErrorCode());
		}

		Apply apply = applyMapper.toApplyEntity(requestBody, applyType, meeting, user, userId);
		return applyRepository.save(apply);
	}

	/**
	 * Fat TX 경로 (부하 테스트 전용)
	 * <p>
	 * develop의 Fat TX와 동일한 DB I/O 패턴(SELECT 4 + INSERT 1)을 재현.
	 * 검증 로직은 의도적으로 생략: (1) private 메서드 접근 불가 (2) 부하 테스트 목적상 불필요.
	 * 핵심은 "단일 TX 내 5개 DB 왕복"의 성능 특성 측정.
	 */
	@Transactional
	public MeetingV2ApplyMeetingResponseDto applyFatTx(MeetingV2ApplyMeetingDto requestBody,
		Integer userId) {

		// SELECT 4회: develop Fat TX와 동일한 DB I/O 패턴
		Meeting meeting = meetingRepository.findByIdOrThrow(requestBody.getMeetingId());
		User user = userRepository.findByIdOrThrow(userId);
		coLeaderRepository.findAllByMeetingId(meeting.getId());
		applyRepository.findAllByMeetingId(meeting.getId());

		// INSERT 1회
		Apply apply = applyMapper.toApplyEntity(requestBody, EnApplyType.APPLY,
			meeting, user, userId);
		Apply savedApply = applyRepository.save(apply);
		return MeetingV2ApplyMeetingResponseDto.of(savedApply.getId());
	}
}
