package org.sopt.makers.crew.main.dining.v2.service;

import org.sopt.makers.crew.main.dining.v2.dto.event.DiningPayerApprovedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningPayerRequestedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiningPayerEventListener {

	private final DiningNotificationSender diningNotificationSender;

	@Async("taskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleDiningPayerRequested(DiningPayerRequestedEvent event) {
		diningNotificationSender.sendPayerRequestedNotification(event);
	}

	@Async("taskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleDiningPayerApproved(DiningPayerApprovedEvent event) {
		diningNotificationSender.sendPayerApprovedNotification(event);
	}

}
