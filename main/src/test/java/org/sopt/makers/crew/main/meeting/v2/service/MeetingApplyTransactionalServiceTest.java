package org.sopt.makers.crew.main.meeting.v2.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.ALREADY_APPLIED_MEETING;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.ApplyTestRepository;
import org.sopt.makers.crew.main.entity.meeting.CoLeaderRepository;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.meeting.v2.dto.ApplyMapper;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2ApplyMeetingDto;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
@DisplayName("MeetingApplyTransactionalService 단위 테스트")
class MeetingApplyTransactionalServiceTest {

	private static final Integer MEETING_ID = 1;
	private static final Integer USER_ID = 2;

	@Mock
	private MeetingRepository meetingRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private CoLeaderRepository coLeaderRepository;
	@Mock
	private ApplyRepository applyRepository;
	@Mock
	private ApplyTestRepository applyTestRepository;
	@Mock
	private ApplyMapper applyMapper;
	@Mock
	private MeetingApplyValidator meetingApplyValidator;
	@Mock
	private MeetingV2ApplyMeetingDto request;
	@Mock
	private Meeting meeting;
	@Mock
	private User user;
	@Mock
	private Apply apply;

	@InjectMocks
	private MeetingApplyTransactionalService service;

	@BeforeEach
	void setUp() {
		given(request.getMeetingId()).willReturn(MEETING_ID);
		given(meetingRepository.findByIdOrThrow(MEETING_ID)).willReturn(meeting);
		given(userRepository.findByIdOrThrow(USER_ID)).willReturn(user);
		given(meeting.getId()).willReturn(MEETING_ID);
		given(coLeaderRepository.findAllByMeetingId(MEETING_ID)).willReturn(List.of());
		given(applyRepository.findAllByMeetingId(MEETING_ID)).willReturn(List.of());
		given(applyMapper.toApplyEntity(eq(request), any(), eq(meeting), eq(user), eq(USER_ID))).willReturn(apply);
	}

	@Test
	@DisplayName("중복 신청 유니크 제약 위반은 이미 신청한 모임 예외로 변환한다")
	void applyGeneral_WhenDuplicateConstraintViolated_ShouldThrowAlreadyAppliedException() {
		DataIntegrityViolationException exception = constraintViolation("meetingid_userid_unique");
		given(applyRepository.saveAndFlush(apply)).willThrow(exception);

		BadRequestException thrown = catchThrowableOfType(
			() -> service.applyGeneral(request, USER_ID), BadRequestException.class);

		assertThat(thrown.getErrorCode()).isEqualTo(ALREADY_APPLIED_MEETING.getErrorCode());
	}

	@Test
	@DisplayName("다른 데이터 무결성 위반은 원본 예외를 다시 던진다")
	void applyGeneral_WhenOtherConstraintViolated_ShouldRethrowOriginalException() {
		DataIntegrityViolationException exception = constraintViolation("apply_user_id_fk");
		given(applyRepository.saveAndFlush(apply)).willThrow(exception);

		assertThatThrownBy(() -> service.applyGeneral(request, USER_ID))
			.isSameAs(exception);
	}

	private DataIntegrityViolationException constraintViolation(String constraintName) {
		ConstraintViolationException cause = new ConstraintViolationException(
			"constraint violation", new SQLException(), "insert into apply", constraintName);
		return new DataIntegrityViolationException("could not execute statement", cause);
	}
}
