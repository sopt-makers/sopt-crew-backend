package org.sopt.makers.crew.main.meetingdemand.v2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemand;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandRepository;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandWaitRepository;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandCommentRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserFixture;
import org.sopt.makers.crew.main.external.notification.PushNotificationService;
import org.sopt.makers.crew.main.external.notification.dto.request.PushNotificationRequestDto;
import org.sopt.makers.crew.main.global.config.PushNotificationProperties;

@ExtendWith(MockitoExtension.class)
class MeetingDemandNotificationSenderTest {

	private static final int MEETING_DEMAND_ID = 10;
	private static final int MEETING_ID = 20;
	private static final int DEMAND_WRITER_ID = 1;
	private static final int MEETING_LEADER_ID = 2;

	@Mock
	private MeetingDemandRepository meetingDemandRepository;

	@Mock
	private MeetingDemandCommentRepository meetingDemandCommentRepository;

	@Mock
	private MeetingDemandWaitRepository meetingDemandWaitRepository;

	@Mock
	private PushNotificationService pushNotificationService;

	private MeetingDemandNotificationSender meetingDemandNotificationSender;
	private MeetingDemand meetingDemand;
	private Meeting meeting;

	@BeforeEach
	void setUp() {
		meetingDemandNotificationSender = new MeetingDemandNotificationSender(
			meetingDemandRepository,
			meetingDemandCommentRepository,
			meetingDemandWaitRepository,
			pushNotificationService,
			new PushNotificationProperties("https://crew.test")
		);

		User demandWriter = UserFixture.createUser(DEMAND_WRITER_ID, "서버", 36);
		User meetingLeader = UserFixture.createUser(MEETING_LEADER_ID, "기획", 36);
		meetingDemand = MeetingDemand.builder()
			.user(demandWriter)
			.shortIntro("러닝 모임 열어주세요")
			.expectation("같이 달릴 모임이 있으면 좋겠어요.")
			.meetingKeywordTypes(List.of())
			.joinInfo(null)
			.build();
		setField(meetingDemand, "id", MEETING_DEMAND_ID);

		meeting = Meeting.builder()
			.user(meetingLeader)
			.meetingDemandId(MEETING_DEMAND_ID)
			.createdGeneration(36)
			.build();
		setField(meeting, "id", MEETING_ID);
	}

	@Test
	@DisplayName("수요 기반 모임 개설 알림은 중복을 제거하고 모임장을 제외해 발송한다.")
	void sendOpenedMeetingNotification_deduplicatesReceiversAndExcludesMeetingLeader() {
		given(meetingDemandRepository.findByIdOrThrow(MEETING_DEMAND_ID)).willReturn(meetingDemand);
		given(meetingDemandCommentRepository.findDistinctUserIdsByMeetingDemandId(MEETING_DEMAND_ID))
			.willReturn(List.of(DEMAND_WRITER_ID, MEETING_LEADER_ID, 3, 3));
		given(meetingDemandWaitRepository.findUserIdsByMeetingDemandId(MEETING_DEMAND_ID))
			.willReturn(List.of(MEETING_LEADER_ID, 4));

		meetingDemandNotificationSender.sendOpenedMeetingNotification(meeting);

		ArgumentCaptor<PushNotificationRequestDto> captor = ArgumentCaptor.forClass(
			PushNotificationRequestDto.class);
		verify(pushNotificationService).sendPushNotification(captor.capture());

		PushNotificationRequestDto request = captor.getValue();
		assertThat(request.getUserIds()).containsExactly("1", "3", "4");
		assertThat(request.getTitle()).isEqualTo("기다리던 모임이 열렸어요");
		assertThat(request.getContent()).isEqualTo("관심을 보였던 수요가 모임으로 개설됐어요.");
		assertThat(request.getCategory()).isEqualTo("NEWS");
		assertThat(request.getWebLink()).isEqualTo("https://crew.test/detail?id=20");
	}

	@Test
	@DisplayName("수신자가 모임장뿐이면 수요 기반 모임 개설 알림을 보내지 않는다.")
	void sendOpenedMeetingNotification_skipsWhenOnlyReceiverIsMeetingLeader() {
		User meetingLeaderDemand = UserFixture.createUser(MEETING_LEADER_ID, "기획", 36);
		MeetingDemand ownDemand = MeetingDemand.builder()
			.user(meetingLeaderDemand)
			.shortIntro("러닝 모임 열어주세요")
			.expectation("같이 달릴 모임이 있으면 좋겠어요.")
			.meetingKeywordTypes(List.of())
			.joinInfo(null)
			.build();
		setField(ownDemand, "id", MEETING_DEMAND_ID);
		given(meetingDemandRepository.findByIdOrThrow(MEETING_DEMAND_ID)).willReturn(ownDemand);
		given(meetingDemandCommentRepository.findDistinctUserIdsByMeetingDemandId(MEETING_DEMAND_ID))
			.willReturn(List.of(MEETING_LEADER_ID));
		given(meetingDemandWaitRepository.findUserIdsByMeetingDemandId(MEETING_DEMAND_ID))
			.willReturn(List.of(MEETING_LEADER_ID));

		meetingDemandNotificationSender.sendOpenedMeetingNotification(meeting);

		verify(pushNotificationService, never()).sendPushNotification(org.mockito.ArgumentMatchers.any());
	}

	@Test
	@DisplayName("기다려요 알림은 수요 작성자에게 수요 상세 링크로 발송한다.")
	void sendWaitNotification_sendsToMeetingDemandWriter() {
		meetingDemandNotificationSender.sendWaitNotification(meetingDemand);

		ArgumentCaptor<PushNotificationRequestDto> captor = ArgumentCaptor.forClass(
			PushNotificationRequestDto.class);
		verify(pushNotificationService).sendPushNotification(captor.capture());

		PushNotificationRequestDto request = captor.getValue();
		assertThat(request.getUserIds()).containsExactly("1");
		assertThat(request.getTitle()).isEqualTo("내가 제안한 모임을 기다려요!");
		assertThat(request.getContent()).isEqualTo("내 제안에 관심을 보인 멤버가 있어요.");
		assertThat(request.getWebLink()).isEqualTo("https://crew.test/suggest/detail?id=10");
	}
}
