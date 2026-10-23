package org.sopt.makers.crew.main.meeting.v2.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.ALREADY_APPLIED_MEETING;

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
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.meeting.v2.dto.ApplyMapper;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2ApplyMeetingDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2ApplyMeetingResponseDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeetingApplyTransactionalService {

	private static final String DUPLICATE_APPLY_CONSTRAINT = "meetingid_userid_unique";

	private final MeetingRepository meetingRepository;
	private final UserRepository userRepository;
	private final CoLeaderRepository coLeaderRepository;
	private final ApplyRepository applyRepository;
	private final ApplyTestRepository applyTestRepository;
	private final ApplyMapper applyMapper;
	private final MeetingApplyValidator meetingApplyValidator;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public MeetingV2ApplyMeetingResponseDto applyGeneral(MeetingV2ApplyMeetingDto requestBody, Integer userId) {
		Meeting meeting = meetingRepository.findByIdOrThrow(requestBody.getMeetingId());
		User user = userRepository.findByIdOrThrow(userId);
		CoLeaders coLeaders = new CoLeaders(coLeaderRepository.findAllByMeetingId(meeting.getId()));

		List<Apply> applies = applyRepository.findAllByMeetingId(meeting.getId());
		meetingApplyValidator.validateGeneralApplyRequest(meeting, user, userId, applies, coLeaders);

		Apply apply = applyMapper.toApplyEntity(requestBody, EnApplyType.APPLY, meeting, user, userId);
		Apply savedApply = saveApply(apply);
		return MeetingV2ApplyMeetingResponseDto.of(savedApply.getId());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public MeetingV2ApplyMeetingResponseDto testApplyGeneral(MeetingV2ApplyMeetingDto requestBody, Integer userId) {
		Meeting meeting = meetingRepository.findByIdOrThrow(requestBody.getMeetingId());
		User user = userRepository.findByIdOrThrow(userId);

		List<ApplyTest> applies = applyTestRepository.findAllByMeetingId(meeting.getId());
		//validateMeetingCapacity(meeting, applies);

		ApplyTest apply = applyMapper.toApplyTestEntity(requestBody, EnApplyType.APPLY, meeting, user, userId);
		ApplyTest savedApply = applyTestRepository.saveAndFlush(apply);
		return MeetingV2ApplyMeetingResponseDto.of(savedApply.getId());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public MeetingV2ApplyMeetingResponseDto applyEvent(MeetingV2ApplyMeetingDto requestBody, Integer userId) {
		Meeting meeting = meetingRepository.findByIdOrThrow(requestBody.getMeetingId());
		User user = userRepository.findByIdOrThrow(userId);
		CoLeaders coLeaders = new CoLeaders(coLeaderRepository.findAllByMeetingId(meeting.getId()));

		List<Apply> applies = applyRepository.findAllByMeetingId(meeting.getId());
		meetingApplyValidator.validateEventApplyRequest(meeting, user, userId, applies, coLeaders);

		Apply apply = applyMapper.toApplyEntity(requestBody, EnApplyType.APPLY, meeting, user, userId);
		Apply savedApply = saveApply(apply);
		return MeetingV2ApplyMeetingResponseDto.of(savedApply.getId());
	}

	private Apply saveApply(Apply apply) {
		try {
			// IDENTITY 전략에서는 save() 시 INSERT되지만, flush 시점을 코드에 명시적으로 드러낸다.
			return applyRepository.saveAndFlush(apply);
		} catch (DataIntegrityViolationException e) {
			if (isDuplicateApplyViolation(e)) {
				throw new BadRequestException(ALREADY_APPLIED_MEETING.getErrorCode());
			}
			throw e;
		}
	}

	private boolean isDuplicateApplyViolation(DataIntegrityViolationException exception) {
		Throwable cause = exception;
		while (cause != null) {
			if (cause instanceof ConstraintViolationException constraintViolation
				&& DUPLICATE_APPLY_CONSTRAINT.equalsIgnoreCase(constraintViolation.getConstraintName())) {
				return true;
			}
			cause = cause.getCause();
		}
		return false;
	}
}
