package org.sopt.makers.crew.main.meeting.v2.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.APPLY_ADMISSION_TIMEOUT;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.INTERNAL_SERVER_ERROR;

import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.sopt.makers.crew.main.global.config.ApplyAdmissionProperties;
import org.sopt.makers.crew.main.global.exception.ServerException;
import org.sopt.makers.crew.main.global.exception.ServiceUnavailableException;
import org.springframework.stereotype.Component;

/**
 * 고정 permit으로 한 JVM의 동시 실행 상한을 보장하기 위해 Semaphore를 사용한다.
 * timeout이 0이면 permit을 얻을 때까지 대기하고, 양수이면 설정된 시간까지만 대기한다.
 * permit은 인스턴스별 HikariCP maximumPoolSize보다 작게 두어 다른 API가 사용할 커넥션을 남겨야 한다.
 * 여러 서버의 전체 진입량은 제한하지 못하므로 전역 제어가 필요하면 분산 Semaphore 구현으로 교체해야 한다.
 */
@Component
public class LocalSemaphoreApplyAdmissionControl implements ApplyAdmissionControl {

	private final boolean enabled;
	private final Semaphore semaphore;
	private final Duration waitTimeout;

	public LocalSemaphoreApplyAdmissionControl(ApplyAdmissionProperties properties) {
		this.enabled = properties.isEnabled();
		// 공정 모드는 JVM 내 대기 스레드의 기아를 막지만 네트워크 도착 순서나 전역 순서를 보장하지 않는다.
		this.semaphore = new Semaphore(properties.getPermits(), true);
		this.waitTimeout = properties.getWaitTimeout();
	}

	@Override
	public <T> T execute(Supplier<T> task) {
		if (!enabled) {
			return task.get();
		}

		boolean acquired;
		try {
			acquired = acquirePermit();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ServerException(INTERNAL_SERVER_ERROR.getErrorCode());
		}

		if (!acquired) {
			throw new ServiceUnavailableException(APPLY_ADMISSION_TIMEOUT.getErrorCode());
		}

		try {
			return task.get();
		} finally {
			semaphore.release();
		}
	}

	private boolean acquirePermit() throws InterruptedException {
		if (waitTimeout.isZero()) {
			semaphore.acquire();
			return true;
		}
		return semaphore.tryAcquire(waitTimeout.toNanos(), TimeUnit.NANOSECONDS);
	}
}
