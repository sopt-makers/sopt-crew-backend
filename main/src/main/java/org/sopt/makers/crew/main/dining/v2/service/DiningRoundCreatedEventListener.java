package org.sopt.makers.crew.main.dining.v2.service;

import org.sopt.makers.crew.main.dining.v2.dto.event.DiningRoundCreatedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiningRoundCreatedEventListener {

	private final DiningNotificationSender diningNotificationSender;

	@Async("taskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleDiningRoundCreatedEvent(DiningRoundCreatedEvent event) {
		diningNotificationSender.sendNewRoundNotification(event.meetingId(), event.roundId());
	}
}
