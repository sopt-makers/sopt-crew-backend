package org.sopt.makers.crew.main.meetingdemand.v2.service;

import org.sopt.makers.crew.main.meetingdemand.v2.dto.event.MeetingDemandOpenedNotificationEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MeetingDemandOpenedNotificationEventListener {

	private final MeetingDemandOpenedNotificationService meetingDemandOpenedNotificationService;

	@Async("taskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleMeetingDemandOpenedNotificationEvent(MeetingDemandOpenedNotificationEvent event) {
		meetingDemandOpenedNotificationService.sendNotification(event.meetingId());
	}
}
