package org.sopt.makers.crew.main.meetingdemand.v2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandOpenedNotification;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandOpenedNotificationRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserFixture;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.event.MeetingDemandOpenedNotificationEvent;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class MeetingDemandOpenedNotificationServiceTest {

	private static final int MEETING_ID = 20;
	private static final int MEETING_DEMAND_ID = 10;
	private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 7, 12, 0);
	private static final LocalDateTime LATE_NIGHT = LocalDateTime.of(2026, 7, 7, 23, 0);

	@Mock
	private MeetingDemandOpenedNotificationRepository meetingDemandOpenedNotificationRepository;

	@Mock
	private MeetingRepository meetingRepository;

	@Mock
	private MeetingDemandNotificationSender meetingDemandNotificationSender;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@Mock
	private Time time;

	private MeetingDemandOpenedNotificationService meetingDemandOpenedNotificationService;
	private Meeting meeting;

	@BeforeEach
	void setUp() {
		meetingDemandOpenedNotificationService = new MeetingDemandOpenedNotificationService(
			meetingDemandOpenedNotificationRepository,
			meetingRepository,
			meetingDemandNotificationSender,
			eventPublisher,
			time
		);

		meeting = createApplyAbleMeeting(NOW);
	}

	@Test
	@DisplayName("수요 기반 모임이 신청 가능 상태로 생성되면 알림 이력을 만들고 즉시 발송 이벤트를 발행한다.")
	void register_applyAbleMeetingPublishesNotificationEvent() {
		MeetingDemandOpenedNotification notification = MeetingDemandOpenedNotification.builder()
			.meetingId(MEETING_ID)
			.build();
		given(time.now()).willReturn(NOW);
		given(meetingDemandOpenedNotificationRepository.findByMeetingId(MEETING_ID)).willReturn(Optional.empty());
		given(meetingDemandOpenedNotificationRepository.save(any(MeetingDemandOpenedNotification.class)))
			.willReturn(notification);

		meetingDemandOpenedNotificationService.register(meeting);

		verify(meetingDemandOpenedNotificationRepository).save(any(MeetingDemandOpenedNotification.class));
		ArgumentCaptor<MeetingDemandOpenedNotificationEvent> captor = ArgumentCaptor.forClass(
			MeetingDemandOpenedNotificationEvent.class);
		verify(eventPublisher).publishEvent(captor.capture());
		assertThat(captor.getValue().meetingId()).isEqualTo(MEETING_ID);
	}

	@Test
	@DisplayName("수요 기반 모임이 22시 이후 신청 가능 상태로 생성되어도 즉시 발송 이벤트를 발행한다.")
	void register_applyAbleMeetingAfterQuietHoursPublishesNotificationEvent() {
		Meeting lateNightMeeting = createApplyAbleMeeting(LATE_NIGHT);
		MeetingDemandOpenedNotification notification = MeetingDemandOpenedNotification.builder()
			.meetingId(MEETING_ID)
			.build();
		given(time.now()).willReturn(LATE_NIGHT);
		given(meetingDemandOpenedNotificationRepository.findByMeetingId(MEETING_ID)).willReturn(Optional.empty());
		given(meetingDemandOpenedNotificationRepository.save(any(MeetingDemandOpenedNotification.class)))
			.willReturn(notification);

		meetingDemandOpenedNotificationService.register(lateNightMeeting);

		ArgumentCaptor<MeetingDemandOpenedNotificationEvent> captor = ArgumentCaptor.forClass(
			MeetingDemandOpenedNotificationEvent.class);
		verify(eventPublisher).publishEvent(captor.capture());
		assertThat(captor.getValue().meetingId()).isEqualTo(MEETING_ID);
	}

	@Test
	@DisplayName("모집 시작 전 수요 기반 모임은 이력만 만들고 즉시 발송하지 않는다.")
	void register_beforeStartMeetingDoesNotPublishNotificationEvent() {
		Meeting beforeStartMeeting = Meeting.builder()
			.user(UserFixture.createUser(2, "기획", 36))
			.meetingDemandId(MEETING_DEMAND_ID)
			.startDate(NOW.plusHours(1))
			.endDate(NOW.plusHours(2))
			.createdGeneration(36)
			.build();
		setField(beforeStartMeeting, "id", MEETING_ID);
		given(time.now()).willReturn(NOW);
		given(meetingDemandOpenedNotificationRepository.findByMeetingId(MEETING_ID)).willReturn(Optional.empty());
		given(meetingDemandOpenedNotificationRepository.save(any(MeetingDemandOpenedNotification.class)))
			.willAnswer(invocation -> invocation.getArgument(0));

		meetingDemandOpenedNotificationService.register(beforeStartMeeting);

		verify(meetingDemandOpenedNotificationRepository).save(any(MeetingDemandOpenedNotification.class));
		verify(eventPublisher, never()).publishEvent(any());
	}

	@Test
	@DisplayName("모집 종료된 수요 기반 모임은 알림 이력을 만들지 않는다.")
	void register_recruitmentCompleteMeetingSkipsNotification() {
		Meeting completedMeeting = Meeting.builder()
			.user(UserFixture.createUser(2, "기획", 36))
			.meetingDemandId(MEETING_DEMAND_ID)
			.startDate(NOW.minusHours(2))
			.endDate(NOW.minusHours(1))
			.createdGeneration(36)
			.build();
		given(time.now()).willReturn(NOW);

		meetingDemandOpenedNotificationService.register(completedMeeting);

		verify(meetingDemandOpenedNotificationRepository, never()).save(any());
		verify(eventPublisher, never()).publishEvent(any());
	}

	@Test
	@DisplayName("발송 가능한 pending 알림은 푸시 발송 후 sentAt을 기록한다.")
	void sendNotification_sendsPushAndMarksSent() {
		MeetingDemandOpenedNotification notification = MeetingDemandOpenedNotification.builder()
			.meetingId(MEETING_ID)
			.build();
		given(time.now()).willReturn(NOW);
		given(meetingDemandOpenedNotificationRepository.findByMeetingId(MEETING_ID))
			.willReturn(Optional.of(notification));
		given(meetingRepository.findByIdOrThrow(MEETING_ID)).willReturn(meeting);

		meetingDemandOpenedNotificationService.sendNotification(MEETING_ID);

		verify(meetingDemandNotificationSender).sendOpenedMeetingNotification(meeting);
		assertThat(notification.getSentAt()).isEqualTo(NOW);
	}

	@Test
	@DisplayName("22시 이후에도 pending 알림은 푸시 발송 후 sentAt을 기록한다.")
	void sendNotification_afterQuietHoursSendsPushAndMarksSent() {
		Meeting lateNightMeeting = createApplyAbleMeeting(LATE_NIGHT);
		MeetingDemandOpenedNotification notification = MeetingDemandOpenedNotification.builder()
			.meetingId(MEETING_ID)
			.build();
		given(time.now()).willReturn(LATE_NIGHT);
		given(meetingDemandOpenedNotificationRepository.findByMeetingId(MEETING_ID))
			.willReturn(Optional.of(notification));
		given(meetingRepository.findByIdOrThrow(MEETING_ID)).willReturn(lateNightMeeting);

		meetingDemandOpenedNotificationService.sendNotification(MEETING_ID);

		verify(meetingDemandNotificationSender).sendOpenedMeetingNotification(lateNightMeeting);
		assertThat(notification.getSentAt()).isEqualTo(LATE_NIGHT);
	}

	@Test
	@DisplayName("스케줄러용 pending 발송은 신청 가능한 미발송 알림만 처리한다.")
	void sendPendingNotifications_sendsApplyAblePendingNotifications() {
		MeetingDemandOpenedNotification notification = MeetingDemandOpenedNotification.builder()
			.meetingId(MEETING_ID)
			.build();
		given(time.now()).willReturn(NOW);
		given(meetingDemandOpenedNotificationRepository.findAllUnsentApplyAble(NOW))
			.willReturn(List.of(notification));
		given(meetingDemandOpenedNotificationRepository.findByMeetingId(MEETING_ID))
			.willReturn(Optional.of(notification));
		given(meetingRepository.findByIdOrThrow(MEETING_ID)).willReturn(meeting);

		meetingDemandOpenedNotificationService.sendPendingNotifications();

		verify(meetingDemandNotificationSender).sendOpenedMeetingNotification(meeting);
		assertThat(notification.getSentAt()).isEqualTo(NOW);
	}

	@Test
	@DisplayName("스케줄러용 pending 발송은 22시 이후에도 신청 가능한 미발송 알림을 처리한다.")
	void sendPendingNotifications_afterQuietHoursSendsApplyAblePendingNotifications() {
		Meeting lateNightMeeting = createApplyAbleMeeting(LATE_NIGHT);
		MeetingDemandOpenedNotification notification = MeetingDemandOpenedNotification.builder()
			.meetingId(MEETING_ID)
			.build();
		given(time.now()).willReturn(LATE_NIGHT);
		given(meetingDemandOpenedNotificationRepository.findAllUnsentApplyAble(LATE_NIGHT))
			.willReturn(List.of(notification));
		given(meetingDemandOpenedNotificationRepository.findByMeetingId(MEETING_ID))
			.willReturn(Optional.of(notification));
		given(meetingRepository.findByIdOrThrow(MEETING_ID)).willReturn(lateNightMeeting);

		meetingDemandOpenedNotificationService.sendPendingNotifications();

		verify(meetingDemandNotificationSender).sendOpenedMeetingNotification(lateNightMeeting);
		assertThat(notification.getSentAt()).isEqualTo(LATE_NIGHT);
	}

	private Meeting createApplyAbleMeeting(LocalDateTime now) {
		User meetingLeader = UserFixture.createUser(2, "기획", 36);
		Meeting applyAbleMeeting = Meeting.builder()
			.user(meetingLeader)
			.meetingDemandId(MEETING_DEMAND_ID)
			.startDate(now.minusHours(1))
			.endDate(now.plusHours(1))
			.createdGeneration(36)
			.build();
		setField(applyAbleMeeting, "id", MEETING_ID);

		return applyAbleMeeting;
	}
}
