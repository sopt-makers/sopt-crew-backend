package org.sopt.makers.crew.main.dining.v2.service;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 회차 자동 확정. 회식 일자 다음 날 오전 11:59 에 마감하지 않은 회차를 일괄 마감(확정)한다.
 * 서버가 그 시각에 멈춰 있었더라도 다음 실행에서 회식 일자가 지난 미마감 회차를 함께 처리한다.
 * 마감 조건(미마감)으로만 갱신하므로 여러 인스턴스에서 동시에 실행돼도 결과가 같다.
 * lambda-dev 는 요청이 있을 때만 실행되는 환경이라 정해진 시각 실행을 보장할 수 없어 제외한다.
 */
@Slf4j
@Component
@Profile({"local", "dev", "prod", "traffic"})
@RequiredArgsConstructor
public class DiningRoundAutoCloseScheduler {

	private final DiningV2Service diningV2Service;

	@Scheduled(cron = "0 59 11 * * *", zone = "Asia/Seoul")
	public void closeExpiredRounds() {
		int closedCount = diningV2Service.closeExpiredRounds();
		log.info("[회식 회차 자동 확정] {}개 회차를 마감했습니다.", closedCount);
	}
}
