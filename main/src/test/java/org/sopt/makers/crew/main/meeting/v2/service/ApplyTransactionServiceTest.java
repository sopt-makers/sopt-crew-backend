package org.sopt.makers.crew.main.meeting.v2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.global.metrics.SpikeDiagnosticProperties;
import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyType;
import org.sopt.makers.crew.main.entity.meeting.CoLeaderRepository;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.meeting.v2.dto.ApplyMapper;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2ApplyMeetingDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2ApplyMeetingResponseDto;

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

		doReturn(meeting).when(meetingRepository).findByIdOrThrow(meetingId);
		doReturn(user).when(userRepository).findByIdOrThrow(userId);
		doReturn(meetingId).when(meeting).getId();
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
		verify(coLeaderRepository).existsByMeetingIdAndUserId(meetingId, userId);
		verify(applyRepository).countByMeetingId(meetingId);
		verify(coLeaderRepository, never()).findAllByMeetingId(anyInt());
		verify(applyRepository, never()).findAllByMeetingId(anyInt());
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

		doReturn(meeting).when(meetingRepository).findByIdOrThrow(meetingId);
		doReturn(user).when(userRepository).findByIdOrThrow(userId);
		doReturn(meetingId).when(meeting).getId();
		doReturn(true).when(coLeaderRepository).existsByMeetingIdAndUserId(meetingId, userId);
		doReturn(9999).when(applyRepository).countByMeetingId(meetingId);
		doReturn(mappedApply).when(applyMapper).toApplyEntity(requestBody, EnApplyType.APPLY, meeting, user, userId);
		doReturn(savedApply).when(applyRepository).save(mappedApply);
		doReturn(1000).when(savedApply).getId();

		// when & then
		assertThatCode(() -> applyTransactionService.applyFatTx(requestBody, userId, "fat", "on"))
			.doesNotThrowAnyException();
		verify(applyRepository, times(1)).save(mappedApply);
	}
}
