package org.sopt.makers.crew.main.dining.v2.service;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 회식 마감 안내(당일 23:00)와 전날 리마인드(16:00) 발송.
 * 한 회식의 발송 실패가 다른 회식 발송을 막지 않으며, push 서버 실패(false 반환)와 예외를 함께 실패로 집계한다.
 * lambda-dev 는 요청이 있을 때만 실행되는 환경이라 정해진 시각 실행을 보장할 수 없어 제외한다. (자동 확정 스케줄러와 동일)
 * TODO: 발송 이력이 없어 인스턴스가 여러 대면 중복 발송된다. 현재 prod 는 1대(blue-green 교체)이며, 늘어나면 발송 선점 처리가 필요하다.
 */
@Slf4j
@Component
@Profile({"local", "dev", "prod", "traffic"})
@RequiredArgsConstructor
public class DiningNotificationScheduler {

	private final DiningReminderService diningReminderService;
	private final DiningNotificationSender diningNotificationSender;

	@Scheduled(cron = "0 0 23 * * *", zone = "Asia/Seoul")
	public void sendCloseGuideNotifications() {
		sendEach("마감 안내", diningReminderService.findCloseGuideTargets(),
			diningNotificationSender::sendCloseGuideNotification);
	}

	@Scheduled(cron = "0 0 16 * * *", zone = "Asia/Seoul")
	public void sendReminderNotifications() {
		sendEach("전날 리마인드", diningReminderService.findReminderTargets(),
			diningNotificationSender::sendReminderNotification);
	}

	private <T> void sendEach(String name, List<T> targets, Predicate<T> sender) {
		int failedCount = 0;
		for (T target : targets) {
			try {
				if (!sender.test(target)) {
					failedCount++;
					log.warn("[회식 {} 알림] push 서버 발송 실패: {}", name, target);
				}
			} catch (RuntimeException exception) {
				failedCount++;
				log.error("[회식 {} 알림] 발송 실패: {}", name, target, exception);
			}
		}
		log.info("[회식 {} 알림] 대상 {}개 중 {}개 실패", name, targets.size(), failedCount);
	}
}
