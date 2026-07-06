package org.sopt.makers.crew.main.meetingdemandcomment.v2.service;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemand;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserFixture;
import org.sopt.makers.crew.main.external.notification.PushNotificationService;
import org.sopt.makers.crew.main.external.notification.dto.request.PushNotificationRequestDto;
import org.sopt.makers.crew.main.global.config.PushNotificationProperties;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.request.MeetingDemandCommentV2MentionUserInCommentRequestDto;

@ExtendWith(MockitoExtension.class)
class MeetingDemandCommentNotificationSenderTest {

	private static final int MEETING_DEMAND_ID = 10;
	private static final int DEMAND_WRITER_ID = 1;
	private static final int COMMENT_WRITER_ID = 2;

	@Mock
	private PushNotificationService pushNotificationService;

	private MeetingDemandCommentNotificationSender meetingDemandCommentNotificationSender;
	private MeetingDemand meetingDemand;

	@BeforeEach
	void setUp() {
		meetingDemandCommentNotificationSender = new MeetingDemandCommentNotificationSender(
			pushNotificationService,
			new PushNotificationProperties("https://crew.test")
		);

		User demandWriter = UserFixture.createUser(DEMAND_WRITER_ID, "서버", 36);
		meetingDemand = MeetingDemand.builder()
			.user(demandWriter)
			.shortIntro("러닝 모임 열어주세요")
			.expectation("같이 달릴 모임이 있으면 좋겠어요.")
			.meetingKeywordTypes(List.of())
			.joinInfo(null)
			.build();
		setField(meetingDemand, "id", MEETING_DEMAND_ID);
	}

	@Test
	@DisplayName("댓글 알림은 새 요구사항 문구로 수요 작성자에게 발송한다.")
	void sendCommentNotification_usesNewMessage() {
		meetingDemandCommentNotificationSender.sendCommentNotification(meetingDemand, COMMENT_WRITER_ID);

		ArgumentCaptor<PushNotificationRequestDto> captor = ArgumentCaptor.forClass(
			PushNotificationRequestDto.class);
		verify(pushNotificationService).sendPushNotification(captor.capture());

		PushNotificationRequestDto request = captor.getValue();
		assertThat(request.getUserIds()).containsExactly("1");
		assertThat(request.getTitle()).isEqualTo("내가 만든 모임 수요에 댓글이 달렸어요");
		assertThat(request.getContent()).isEqualTo("새로운 댓글이 달렸어요.");
		assertThat(request.getWebLink()).isEqualTo("https://crew.test/meeting-demand?id=10");
	}

	@Test
	@DisplayName("멘션 알림도 댓글 알림과 동일한 문구로 발송한다.")
	void sendMentionNotification_usesSameCommentMessage() {
		MeetingDemandCommentV2MentionUserInCommentRequestDto requestBody =
			new MeetingDemandCommentV2MentionUserInCommentRequestDto(
				MEETING_DEMAND_ID,
				"@테스트 유저 멘션 댓글",
				List.of(3L, 4L)
			);

		meetingDemandCommentNotificationSender.sendMentionNotification(requestBody);

		ArgumentCaptor<PushNotificationRequestDto> captor = ArgumentCaptor.forClass(
			PushNotificationRequestDto.class);
		verify(pushNotificationService).sendPushNotification(captor.capture());

		PushNotificationRequestDto request = captor.getValue();
		assertThat(request.getUserIds()).containsExactly("3", "4");
		assertThat(request.getTitle()).isEqualTo("내가 만든 모임 수요에 댓글이 달렸어요");
		assertThat(request.getContent()).isEqualTo("새로운 댓글이 달렸어요.");
		assertThat(request.getWebLink()).isEqualTo("https://crew.test/meeting-demand?id=10");
	}
}
