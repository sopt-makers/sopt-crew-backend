package org.sopt.makers.crew.main.meetingdemand.v2.service;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@Profile({"local", "dev", "prod", "traffic", "lambda-dev"})
@RequiredArgsConstructor
public class MeetingDemandOpenedNotificationScheduler {

	private final MeetingDemandOpenedNotificationService meetingDemandOpenedNotificationService;

	@Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
	public void sendPendingNotifications() {
		meetingDemandOpenedNotificationService.sendPendingNotifications();
	}
}
