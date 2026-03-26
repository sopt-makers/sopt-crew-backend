package org.sopt.makers.crew.main.meeting.v2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.global.metrics.SpikeDiagnosticProperties;
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
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class ApplyTransactionServiceTest {

	@Mock
	private ApplyRepository applyRepository;

	@Mock
	private MeetingRepository meetingRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private CoLeaderRepository coLeaderRepository;

	@Mock
	private ApplyMapper applyMapper;

	@Mock
	private EntityManager entityManager;

	@Spy
	private SpikeApplyProfiler spikeApplyProfiler = new SpikeApplyProfiler(new SpikeDiagnosticProperties());

	@InjectMocks
	private ApplyTransactionService applyTransactionService;

	@Test
	void applyFatTx_스칼라_조회로_저장한다() {
		// given
		Integer meetingId = 1;
		Integer userId = 10;
		MeetingV2ApplyMeetingDto requestBody = new MeetingV2ApplyMeetingDto(meetingId, "지원합니다.");

		Meeting meeting = mock(Meeting.class);
		User user = mock(User.class);
		Apply mappedApply = mock(Apply.class);
		Apply savedApply = mock(Apply.class);

		doReturn(meeting).when(entityManager).getReference(Meeting.class, meetingId);
		doReturn(user).when(entityManager).getReference(User.class, userId);
		doReturn(false).when(coLeaderRepository).existsByMeetingIdAndUserId(meetingId, userId);
		doReturn(230).when(applyRepository).countByMeetingId(meetingId);
		doReturn(mappedApply).when(applyMapper).toApplyEntity(requestBody, EnApplyType.APPLY, meeting, user, userId);
		doReturn(savedApply).when(applyRepository).save(mappedApply);
		doReturn(999).when(savedApply).getId();

		// when
		MeetingV2ApplyMeetingResponseDto response = applyTransactionService.applyFatTx(requestBody, userId, "fat",
			"on");

		// then
		assertThat(response.getApplyId()).isEqualTo(999);
		verify(entityManager).getReference(Meeting.class, meetingId);
		verify(entityManager).getReference(User.class, userId);
		verify(coLeaderRepository).existsByMeetingIdAndUserId(meetingId, userId);
		verify(applyRepository).countByMeetingId(meetingId);
		verify(applyRepository).flush();
		verify(coLeaderRepository, never()).findAllByMeetingId(anyInt());
		verify(applyRepository, never()).findAllByMeetingId(anyInt());
		verify(meetingRepository, never()).findByIdOrThrow(anyInt());
		verify(userRepository, never()).findByIdOrThrow(anyInt());
		verify(applyRepository).save(mappedApply);
	}

	@Test
	void applyFatTx_스칼라_조회_결과와_무관하게_저장한다() {
		// given
		Integer meetingId = 2;
		Integer userId = 20;
		MeetingV2ApplyMeetingDto requestBody = new MeetingV2ApplyMeetingDto(meetingId, "지원합니다.");

		Meeting meeting = mock(Meeting.class);
		User user = mock(User.class);
		Apply mappedApply = mock(Apply.class);
		Apply savedApply = mock(Apply.class);

		doReturn(meeting).when(entityManager).getReference(Meeting.class, meetingId);
		doReturn(user).when(entityManager).getReference(User.class, userId);
		doReturn(true).when(coLeaderRepository).existsByMeetingIdAndUserId(meetingId, userId);
		doReturn(9999).when(applyRepository).countByMeetingId(meetingId);
		doReturn(mappedApply).when(applyMapper).toApplyEntity(requestBody, EnApplyType.APPLY, meeting, user, userId);
		doReturn(savedApply).when(applyRepository).save(mappedApply);
		doReturn(1000).when(savedApply).getId();

		// when & then
		assertThatCode(() -> applyTransactionService.applyFatTx(requestBody, userId, "fat", "on"))
			.doesNotThrowAnyException();
		verify(applyRepository, times(1)).save(mappedApply);
		verify(applyRepository, times(1)).flush();
	}

	@Test
	void saveApply_중복_유니크_위반을_도메인_예외로_매핑한다() {
		Integer meetingId = 1;
		Integer userId = 10;
		MeetingV2ApplyMeetingDto requestBody = new MeetingV2ApplyMeetingDto(meetingId, "지원합니다.");

		Meeting meeting = mock(Meeting.class);
		User user = mock(User.class);
		Apply mappedApply = mock(Apply.class);

		doReturn(meetingId).when(meeting).getId();
		doReturn(10).when(meeting).getCapacity();
		doReturn(0).when(applyRepository).countByMeetingIdAndStatus(meetingId, EnApplyStatus.APPROVE);
		doReturn(mappedApply).when(applyMapper).toApplyEntity(requestBody, EnApplyType.APPLY, meeting, user, userId);
		doThrow(new DataIntegrityViolationException("constraint [UQ_apply_meeting_user]")).when(applyRepository)
			.save(mappedApply);

		assertThatThrownBy(() -> applyTransactionService.saveApply(requestBody, EnApplyType.APPLY, meeting, user, userId,
			"fat", "on"))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("이미 지원한 모임입니다.");

		verify(applyRepository, never()).flush();
	}
}
