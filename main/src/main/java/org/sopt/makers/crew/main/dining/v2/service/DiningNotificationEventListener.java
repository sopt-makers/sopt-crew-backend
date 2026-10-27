package org.sopt.makers.crew.main.dining.v2.service;

import org.sopt.makers.crew.main.dining.v2.dto.event.DiningAttendeeAddedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningAttendeeRemovedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningCreatedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningDeletedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningRoundDeletedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningRoundUpdatedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningUpdatedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

/**
 * 회식 등록·수정·삭제와 참석자 직접 추가·삭제 알림. 커밋된 경우에만 비동기로 발송한다.
 */
@Component
@RequiredArgsConstructor
public class DiningNotificationEventListener {

	private final DiningNotificationSender diningNotificationSender;

	@Async("taskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleDiningCreated(DiningCreatedEvent event) {
		diningNotificationSender.sendDiningCreatedNotification(event);
	}

	@Async("taskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleDiningUpdated(DiningUpdatedEvent event) {
		diningNotificationSender.sendDiningUpdatedNotification(event);
	}

	@Async("taskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleRoundUpdated(DiningRoundUpdatedEvent event) {
		diningNotificationSender.sendRoundUpdatedNotification(event);
	}

	@Async("taskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleDiningDeleted(DiningDeletedEvent event) {
		diningNotificationSender.sendDiningDeletedNotification(event);
	}

	@Async("taskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleRoundDeleted(DiningRoundDeletedEvent event) {
		diningNotificationSender.sendRoundDeletedNotification(event);
	}

	@Async("taskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleAttendeeAdded(DiningAttendeeAddedEvent event) {
		diningNotificationSender.sendAttendeeAddedNotification(event);
	}

	@Async("taskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleAttendeeRemoved(DiningAttendeeRemovedEvent event) {
		diningNotificationSender.sendAttendeeRemovedNotification(event);
	}
}
