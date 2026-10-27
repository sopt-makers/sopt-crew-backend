package org.sopt.makers.crew.main.dining.v2.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningAttendeeAddedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningAttendeeRemovedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningCreatedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningDeletedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningRoundDeletedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningRoundUpdatedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningUpdatedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.notification.DiningCloseGuideTarget;
import org.sopt.makers.crew.main.dining.v2.dto.notification.DiningReminderTarget;
import org.sopt.makers.crew.main.entity.dining.DiningRoundRepository;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.external.notification.PushNotificationService;
import org.sopt.makers.crew.main.external.notification.dto.request.PushNotificationRequestDto;
import org.sopt.makers.crew.main.global.config.PushNotificationProperties;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 회식 등록·수정·삭제, 참석자 직접 추가·삭제 알림의 커밋 후 발송, 문구, 이동 경로, 수신자 처리.
 */
class DiningNotificationTest {

	private static final String DETAIL = "https://crew.test/dining/detail?id=1";
	private static final String TAB = "https://crew.test/dining";

	static Stream<Arguments> snapshotNotifications() {
		return Stream.of(
			Arguments.of(new DiningUpdatedEvent(1, "서버 회식", List.of(3, 4, 3)), new String[] {"3", "4"},
				"회식 정보 수정", "[서버 회식] 변경된 내용을 확인해주세요.", TAB),
			Arguments.of(new DiningRoundUpdatedEvent(1, "서버 회식", 2, List.of(3, 4)), new String[] {"3", "4"},
				"회차 정보 수정", "[서버 회식] 2차 정보가 수정됐어요. 변경된 내용을 확인해주세요.", DETAIL),
			Arguments.of(new DiningDeletedEvent(1, "서버 회식", List.of(3, 4)), new String[] {"3", "4"},
				"회식 삭제", "[서버 회식] 개설자가 회식을 삭제했어요.", TAB),
			Arguments.of(new DiningRoundDeletedEvent(1, "서버 회식", 2, List.of(3)), new String[] {"3"},
				"회차 삭제", "[서버 회식] 2차 회식이 삭제됐어요.", DETAIL),
			Arguments.of(new DiningAttendeeAddedEvent(1, "서버 회식", 2, 3), new String[] {"3"},
				"회식 참석자 추가", "[서버 회식] 2차 참석자로 추가됐어요.", DETAIL),
			Arguments.of(new DiningAttendeeRemovedEvent(1, "서버 회식", 2, 3), new String[] {"3"},
				"회식 참석자 제외", "[서버 회식] 2차 참석자에서 제외됐어요.", DETAIL)
		);
	}

	@ParameterizedTest
	@MethodSource("snapshotNotifications")
	@DisplayName("트랜잭션에서 확정한 수신자에게 커밋 후에만 확정 문구와 경로로 보낸다. (중복 수신자는 1건)")
	void sendsOnlyAfterCommit(Object event, String[] receivers, String title, String content, String link) {
		try (var context = new AnnotationConfigApplicationContext(Config.class)) {
			var push = context.getBean(PushNotificationService.class);
			transaction().executeWithoutResult(status -> {
				context.publishEvent(event);
				verifyNoInteractions(push);
			});
			var request = captureRequest(push);
			assertThat(request.getUserIds()).containsExactly(receivers);
			assertThat(request.getTitle()).isEqualTo(title);
			assertThat(request.getContent()).isEqualTo(content);
			assertThat(request.getWebLink()).isEqualTo(link);
		}
	}

	@ParameterizedTest
	@MethodSource("snapshotNotifications")
	@DisplayName("롤백되거나 트랜잭션 밖에서 발행되면 보내지 않는다.")
	void doesNotSendOnRollbackOrWithoutTransaction(Object event, String[] receivers, String title, String content,
		String link) {
		try (var context = new AnnotationConfigApplicationContext(Config.class)) {
			transaction().executeWithoutResult(status -> {
				context.publishEvent(event);
				status.setRollbackOnly();
			});
			context.publishEvent(event);
			verifyNoInteractions(context.getBean(PushNotificationService.class));
		}
	}

	@Test
	@DisplayName("수신자가 없으면 보내지 않는다.")
	void skipsEmptyReceivers() {
		try (var context = new AnnotationConfigApplicationContext(Config.class)) {
			transaction().executeWithoutResult(status ->
				context.publishEvent(new DiningRoundDeletedEvent(1, "서버 회식", 2, List.of())));
			verifyNoInteractions(context.getBean(PushNotificationService.class));
		}
	}

	@Test
	@DisplayName("회식 등록은 현재 기수에서 참여 파트에 속한 회원에게만 보내고 개설자는 제외한다.")
	void diningCreated_sendsToActiveGenerationMembersOfJoinableParts() {
		try (var context = new AnnotationConfigApplicationContext(Config.class)) {
			stubDining(context, 1);
			when(context.getBean(UserRepository.class).findAllByActivityGeneration(39)).thenReturn(List.of(
				user(10, new UserActivityVO("서버", 39)),                                  // 개설자
				user(11, new UserActivityVO("서버 파트장", 39)),
				user(12, new UserActivityVO("백엔드", 39)),
				user(13, new UserActivityVO("ios", 39)),
				user(14, new UserActivityVO("디자인", 39)),                               // 참여 파트 아님
				user(15, new UserActivityVO("서버", 38), new UserActivityVO("디자인", 39)), // 현재 기수는 디자인
				user(16, new UserActivityVO("총무", 39), new UserActivityVO("iOS 파트장", 39)),
				user(17, new UserActivityVO("회장", 39))                                  // 파트 없음
			));
			transaction().executeWithoutResult(status -> context.publishEvent(new DiningCreatedEvent(1, "서버 회식", 10,
				List.of(MeetingJoinablePart.SERVER, MeetingJoinablePart.IOS), 39)));

			var request = captureRequest(context.getBean(PushNotificationService.class));
			assertThat(request.getUserIds()).containsExactly("11", "12", "13", "16");
			assertThat(request.getTitle()).isEqualTo("새로운 회식 등록");
			assertThat(request.getContent()).isEqualTo("[서버 회식] 함께할 분은 지금 바로 신청해주세요.");
			assertThat(request.getWebLink()).isEqualTo(DETAIL);
		}
	}

	@Test
	@DisplayName("회식 등록 알림 발송 전에 회식이 삭제됐으면 보내지 않는다.")
	void diningCreated_skipsDeletedDining() {
		try (var context = new AnnotationConfigApplicationContext(Config.class)) {
			when(context.getBean(MeetingRepository.class).findById(1)).thenReturn(Optional.empty());
			transaction().executeWithoutResult(status -> context.publishEvent(new DiningCreatedEvent(1, "서버 회식", 10,
				List.of(MeetingJoinablePart.SERVER), 39)));
			verifyNoInteractions(context.getBean(PushNotificationService.class));
			verifyNoInteractions(context.getBean(UserRepository.class));
		}
	}

	@Test
	@DisplayName("마감 안내는 개설자에게 참석자 관리 경로로, 전날 리마인드는 참석자에게 회식 상세 경로로 보낸다.")
	void scheduledNotifications_useFixedContentAndLinks() {
		try (var context = new AnnotationConfigApplicationContext(Config.class)) {
			var sender = context.getBean(DiningNotificationSender.class);
			var push = context.getBean(PushNotificationService.class);
			sender.sendCloseGuideNotification(new DiningCloseGuideTarget(1, "서버 회식", 10));
			sender.sendReminderNotification(new DiningReminderTarget(1, "서버 회식", List.of(10, 11, 10)));

			var captor = ArgumentCaptor.forClass(PushNotificationRequestDto.class);
			verify(push, times(2)).sendPushNotification(captor.capture());
			var closeGuide = captor.getAllValues().get(0);
			assertThat(closeGuide.getUserIds()).containsExactly("10");
			assertThat(closeGuide.getTitle()).isEqualTo("마감하지 않은 회차");
			assertThat(closeGuide.getContent()).isEqualTo("[서버 회식] 내일 오전 11:59에 자동으로 마감돼요.");
			assertThat(closeGuide.getWebLink()).isEqualTo("https://crew.test/dining/attendees?id=1");
			var reminder = captor.getAllValues().get(1);
			assertThat(reminder.getUserIds()).containsExactly("10", "11");
			assertThat(reminder.getTitle()).isEqualTo("내일 예정된 회식");
			assertThat(reminder.getContent()).isEqualTo("[서버 회식] 회식 정보를 확인해주세요.");
			assertThat(reminder.getWebLink()).isEqualTo(DETAIL);
		}
	}

	@Test
	@DisplayName("스케줄러 발송은 push 서버 결과를 반환하고, 수신자가 없어 보내지 않은 경우는 실패로 보지 않는다.")
	void scheduledNotifications_returnPushResult() {
		try (var context = new AnnotationConfigApplicationContext(Config.class)) {
			var sender = context.getBean(DiningNotificationSender.class);
			var push = context.getBean(PushNotificationService.class);
			var target = new DiningReminderTarget(1, "서버 회식", List.of(10));

			when(push.sendPushNotification(any())).thenReturn(true);
			assertThat(sender.sendReminderNotification(target)).isTrue();
			assertThat(sender.sendCloseGuideNotification(new DiningCloseGuideTarget(1, "서버 회식", 10))).isTrue();

			when(push.sendPushNotification(any())).thenReturn(false);
			assertThat(sender.sendReminderNotification(target)).isFalse();
			assertThat(sender.sendCloseGuideNotification(new DiningCloseGuideTarget(1, "서버 회식", 10))).isFalse();

			assertThat(sender.sendReminderNotification(new DiningReminderTarget(1, "서버 회식", List.of()))).isTrue();
		}
	}

	private static void stubDining(AnnotationConfigApplicationContext context, Integer meetingId) {
		Meeting dining = mock(Meeting.class);
		when(dining.getCategory()).thenReturn(MeetingCategory.DINING);
		when(context.getBean(MeetingRepository.class).findById(meetingId)).thenReturn(Optional.of(dining));
	}

	private static User user(Integer id, UserActivityVO... activities) {
		User user = User.builder().name("회원" + id).orgId(id).activities(List.of(activities)).build();
		ReflectionTestUtils.setField(user, "id", id);
		return user;
	}

	private static PushNotificationRequestDto captureRequest(PushNotificationService push) {
		var captor = ArgumentCaptor.forClass(PushNotificationRequestDto.class);
		verify(push).sendPushNotification(captor.capture());
		return captor.getValue();
	}

	private static TransactionTemplate transaction() {
		return new TransactionTemplate(new DiningPayerNotificationTest.TestTransactionManager());
	}

	@Configuration
	@EnableAsync
	@EnableTransactionManagement
	static class Config {
		@Bean
		SyncTaskExecutor taskExecutor() { return new SyncTaskExecutor(); }
		@Bean
		PushNotificationService push() { return mock(PushNotificationService.class); }
		@Bean
		MeetingRepository meetings() { return mock(MeetingRepository.class); }
		@Bean
		DiningRoundRepository rounds() { return mock(DiningRoundRepository.class); }
		@Bean
		UserRepository users() { return mock(UserRepository.class); }
		@Bean
		DiningNotificationSender sender(MeetingRepository meetings, DiningRoundRepository rounds, UserRepository users,
			PushNotificationService push) {
			var properties = mock(PushNotificationProperties.class);
			when(properties.getPushWebUrl()).thenReturn("https://crew.test");
			return new DiningNotificationSender(meetings, rounds, users, push, properties);
		}
		@Bean
		DiningNotificationEventListener listener(DiningNotificationSender sender) {
			return new DiningNotificationEventListener(sender);
		}
	}
}
