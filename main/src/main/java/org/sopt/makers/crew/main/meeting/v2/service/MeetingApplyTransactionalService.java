package org.sopt.makers.crew.main.meeting.v2.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.List;

import org.hibernate.exception.ConstraintViolationException;
import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.ApplyTest;
import org.sopt.makers.crew.main.entity.apply.ApplyTestRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyType;
import org.sopt.makers.crew.main.entity.meeting.CoLeaderRepository;
import org.sopt.makers.crew.main.entity.meeting.CoLeaders;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.global.annotation.NoExecutionLogging;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.meeting.v2.dto.ApplyMapper;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2ApplyMeetingDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2ApplyMeetingResponseDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 모임 신청 트랜잭션. (#895)
 *
 * <p>{@link MeetingApplyInFlightGuard} 안에서 호출되며, 별도 트랜잭션(REQUIRES_NEW)으로 실행되어
 * guard가 키를 해제하기 전에 커밋된다. 기존 UserLockManager는 트랜잭션 안에서 락을 잡고 커밋 전에 해제해서,
 * 동시 요청이 아직 커밋되지 않은 신청을 보지 못하고 중복 검증을 함께 통과할 수 있었다.</p>
 *
 * <p>조회 순서: {@code userRepository.findByIdOrThrow}는 외부 인증 서버를 HTTP로 호출(최대 5초)한 뒤 DB를 조회한다.
 * 커넥션은 LazyConnectionDataSourceProxy라 첫 SQL에서 획득되므로, 사용자를 모임보다 먼저 조회해야
 * 인증 서버 응답을 기다리는 동안 DB 커넥션을 쥐고 있지 않는다. 모집 오픈 순간 신청이 몰릴 때
 * 커넥션 풀이 인증 서버 응답 대기로 고갈되던 문제의 원인이었다. (#956 부하 테스트)</p>
 */
@Service
@NoExecutionLogging
@RequiredArgsConstructor
public class MeetingApplyTransactionalService {

	static final String APPLY_UNIQUE_INDEX_NAME = "uq_apply_meeting_user";

	private final MeetingRepository meetingRepository;
	private final UserRepository userRepository;
	private final CoLeaderRepository coLeaderRepository;
	private final ApplyRepository applyRepository;
	private final ApplyTestRepository applyTestRepository;
	private final ApplyMapper applyMapper;
	private final MeetingApplyValidator meetingApplyValidator;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public MeetingV2ApplyMeetingResponseDto applyGeneral(MeetingV2ApplyMeetingDto requestBody, Integer userId) {
		// 외부 인증 서버 호출이 DB 커넥션을 쥔 채 일어나지 않도록 첫 SQL보다 먼저 사용자를 조회한다
		User user = userRepository.findByIdOrThrow(userId);
		Meeting meeting = meetingRepository.findByIdOrThrow(requestBody.getMeetingId());
		CoLeaders coLeaders = new CoLeaders(coLeaderRepository.findAllByMeetingId(meeting.getId()));

		List<Apply> applies = applyRepository.findAllByMeetingId(meeting.getId());
		meetingApplyValidator.validateGeneralApplyRequest(meeting, user, userId, applies, coLeaders);

		Apply apply = applyMapper.toApplyEntity(requestBody, EnApplyType.APPLY, meeting, user, userId);
		Apply savedApply = saveApply(apply);
		return MeetingV2ApplyMeetingResponseDto.of(savedApply.getId());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public MeetingV2ApplyMeetingResponseDto applyEvent(MeetingV2ApplyMeetingDto requestBody, Integer userId) {
		// 외부 인증 서버 호출이 DB 커넥션을 쥔 채 일어나지 않도록 첫 SQL보다 먼저 사용자를 조회한다
		User user = userRepository.findByIdOrThrow(userId);
		Meeting meeting = meetingRepository.findByIdOrThrow(requestBody.getMeetingId());
		CoLeaders coLeaders = new CoLeaders(coLeaderRepository.findAllByMeetingId(meeting.getId()));

		List<Apply> applies = applyRepository.findAllByMeetingId(meeting.getId());
		meetingApplyValidator.validateEventApplyRequest(meeting, user, userId, applies, coLeaders);

		Apply apply = applyMapper.toApplyEntity(requestBody, EnApplyType.APPLY, meeting, user, userId);
		Apply savedApply = saveApply(apply);
		return MeetingV2ApplyMeetingResponseDto.of(savedApply.getId());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public MeetingV2ApplyMeetingResponseDto testApplyGeneral(MeetingV2ApplyMeetingDto requestBody, Integer userId) {
		// 외부 인증 서버 호출이 DB 커넥션을 쥔 채 일어나지 않도록 첫 SQL보다 먼저 사용자를 조회한다
		User user = userRepository.findByIdOrThrow(userId);
		Meeting meeting = meetingRepository.findByIdOrThrow(requestBody.getMeetingId());

		ApplyTest apply = applyMapper.toApplyTestEntity(requestBody, EnApplyType.APPLY, meeting, user, userId);
		ApplyTest savedApply = applyTestRepository.save(apply);
		return MeetingV2ApplyMeetingResponseDto.of(savedApply.getId());
	}

	/**
	 * apply("meetingId", "userId") 유니크 인덱스 위반을 기존 '이미 지원한 모임' 에러로 변환한다.
	 * 호출한 쪽의 트랜잭션에 참여한다.
	 */
	public Apply saveApply(Apply apply) {
		try {
			// IDENTITY 전략이라 save()에서도 INSERT되지만, 위반을 이 메서드 안에서 확실히 잡도록 flush를 명시한다.
			return applyRepository.saveAndFlush(apply);
		} catch (DataIntegrityViolationException e) {
			if (isDuplicateApplyViolation(e)) {
				throw new BadRequestException(ALREADY_APPLIED_MEETING.getErrorCode());
			}
			throw e;
		}
	}

	/**
	 * Hibernate가 추출한 제약 이름으로 판별하고, 추출하지 못한 경우에만 에러 메시지로 판별한다.
	 */
	static boolean isDuplicateApplyViolation(DataIntegrityViolationException exception) {
		Throwable cause = exception;
		while (cause != null) {
			if (cause instanceof ConstraintViolationException constraintViolation
				&& constraintViolation.getConstraintName() != null) {
				return APPLY_UNIQUE_INDEX_NAME.equalsIgnoreCase(constraintViolation.getConstraintName());
			}
			cause = cause.getCause();
		}

		String message = exception.getMostSpecificCause().getMessage();
		return message != null && message.contains(APPLY_UNIQUE_INDEX_NAME);
	}
}
