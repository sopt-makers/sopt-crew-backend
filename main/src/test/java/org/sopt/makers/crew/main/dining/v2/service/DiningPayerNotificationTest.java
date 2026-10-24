package org.sopt.makers.crew.main.dining.v2.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningPayerApprovedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningPayerRejectedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningPayerRequestedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningPayerRevokedEvent;
import org.sopt.makers.crew.main.entity.dining.DiningRoundRepository;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.external.notification.PushNotificationService;
import org.sopt.makers.crew.main.external.notification.dto.request.PushNotificationRequestDto;
import org.sopt.makers.crew.main.global.config.PushNotificationProperties;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

class DiningPayerNotificationTest {

	static Stream<Arguments> notifications() {
		return Stream.of(
			Arguments.of(new DiningPayerRequestedEvent(1, 2, 3, 4, "서버 회식", "참석자", 2), "4",
				"[서버 회식] 2차 [참석자]님이 공동 결제자 승인을 요청했어요.\n참석자 관리 화면에서 확인해주세요.",
				"https://crew.test/dining/attendees?id=1#payer-requests"),
			Arguments.of(new DiningPayerApprovedEvent(1, 2, 3, "서버 회식"), "3",
				"[서버 회식] 공동 결제자로 승인됐어요.\n이제 참석자 관리 화면을 이용할 수 있어요.",
				"https://crew.test/dining/attendees?id=1"),
			Arguments.of(new DiningPayerRejectedEvent(1, 2, 3, "서버 회식"), "3",
				"[서버 회식] 공동 결제자 요청이 거절됐어요.", "https://crew.test/dining/detail?id=1"),
			Arguments.of(new DiningPayerRevokedEvent(1, 2, 3, "서버 회식", 2), "3",
				"[서버 회식] 2차 공동 결제자 승인이 해제됐어요.", "https://crew.test/dining/detail?id=1")
		);
	}

	@ParameterizedTest
	@MethodSource("notifications")
	void sendsOnlyAfterCommit_withSnapshotRecipientContentAndLink(Object event, String receiver,
		String content, String link) {
		try (var context = new AnnotationConfigApplicationContext(Config.class)) {
			var push = context.getBean(PushNotificationService.class);
			new TransactionTemplate(new TestTransactionManager()).executeWithoutResult(status -> {
				context.publishEvent(event);
				verifyNoInteractions(push);
			});
			var captor = ArgumentCaptor.forClass(PushNotificationRequestDto.class);
			verify(push).sendPushNotification(captor.capture());
			assertThat(captor.getValue().getUserIds()).containsExactly(receiver);
			assertThat(captor.getValue().getContent()).isEqualTo(content);
			assertThat(captor.getValue().getWebLink()).isEqualTo(link);
			verifyNoInteractions(context.getBean(MeetingRepository.class), context.getBean(DiningRoundRepository.class));
		}
	}

	@ParameterizedTest
	@MethodSource("notifications")
	void doesNotSendOnRollbackOrWithoutTransaction(Object event, String receiver, String content, String link) {
		try (var context = new AnnotationConfigApplicationContext(Config.class)) {
			new TransactionTemplate(new TestTransactionManager()).executeWithoutResult(status -> {
				context.publishEvent(event);
				status.setRollbackOnly();
			});
			context.publishEvent(event);
			verifyNoInteractions(context.getBean(PushNotificationService.class));
		}
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
		DiningNotificationSender sender(MeetingRepository meetings, DiningRoundRepository rounds,
			PushNotificationService push) {
			var properties = mock(PushNotificationProperties.class);
			when(properties.getPushWebUrl()).thenReturn("https://crew.test");
			return new DiningNotificationSender(meetings, rounds, push, properties);
		}
		@Bean
		DiningPayerEventListener listener(DiningNotificationSender sender) {
			return new DiningPayerEventListener(sender);
		}
	}

	/** 외부 DB 없이 실제 Spring 트랜잭션 동기화/이벤트 처리를 검증한다. */
	static class TestTransactionManager extends AbstractPlatformTransactionManager {
		@Override
		protected Object doGetTransaction() { return new Object(); }
		@Override
		protected void doBegin(Object transaction, TransactionDefinition definition) { }
		@Override
		protected void doCommit(DefaultTransactionStatus status) { }
		@Override
		protected void doRollback(DefaultTransactionStatus status) { }
	}
}
