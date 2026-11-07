package org.sopt.makers.crew.main.meeting.v2.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.global.exception.LockedException;

/**
 * MeetingApplyInFlightGuard의 진입 제어를 통합 환경 없이 단독으로 검증한다.
 * MeetingV2ConcurrencyTest는 최종 저장 건수만 확인하므로 DB unique index만으로도 통과할 수 있다.
 * 여기서는 in-flight guard가 "처리 중인 같은 키"를 DB 접근 이전에 즉시 거절하는지를 직접 증명한다.
 */
@DisplayName("MeetingApplyInFlightGuard 단위 테스트")
class MeetingApplyInFlightGuardTest {

	@Test
	@DisplayName("같은 (meetingId, userId)가 처리 중이면 두 번째 요청은 즉시 거절된다")
	void execute_WhenSameKeyInFlight_ShouldRejectImmediately() throws InterruptedException {
		MeetingApplyInFlightGuard inFlightGuard = new MeetingApplyInFlightGuard();
		CountDownLatch entered = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		ExecutorService executor = Executors.newSingleThreadExecutor();

		try {
			// 첫 요청이 (1, 1) 키를 잡은 채 task 안에서 대기하게 한다.
			executor.submit(() -> inFlightGuard.execute(1, 1, () -> {
				entered.countDown();
				await(release);
				return "first";
			}));
			assertThat(entered.await(1, TimeUnit.SECONDS)).isTrue();

			// 같은 키의 두 번째 요청은 기다리지 않고 LockedException으로 즉시 실패한다.
			// 대기형 락으로 회귀하면 여기서 블로킹되므로 시간 상한을 둔다.
			assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
				assertThatThrownBy(() -> inFlightGuard.execute(1, 1, () -> "second"))
					.isInstanceOf(LockedException.class));
		} finally {
			release.countDown();
			executor.shutdown();
			assertThat(executor.awaitTermination(1, TimeUnit.SECONDS)).isTrue();
		}
	}

	@Test
	@DisplayName("서로 다른 키는 서로를 막지 않는다")
	void execute_WhenDifferentKey_ShouldNotBlock() throws InterruptedException {
		MeetingApplyInFlightGuard inFlightGuard = new MeetingApplyInFlightGuard();
		CountDownLatch entered = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		ExecutorService executor = Executors.newSingleThreadExecutor();

		try {
			// (1, 1) 키를 잡고 대기하는 동안
			executor.submit(() -> inFlightGuard.execute(1, 1, () -> {
				entered.countDown();
				await(release);
				return "first";
			}));
			assertThat(entered.await(1, TimeUnit.SECONDS)).isTrue();

			// 다른 모임(2, 1)과 다른 사용자(1, 2)는 막히지 않고 즉시 통과한다.
			assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
				assertThat(inFlightGuard.execute(2, 1, () -> "otherMeeting")).isEqualTo("otherMeeting");
				assertThat(inFlightGuard.execute(1, 2, () -> "otherUser")).isEqualTo("otherUser");
			});
		} finally {
			release.countDown();
			executor.shutdown();
			assertThat(executor.awaitTermination(1, TimeUnit.SECONDS)).isTrue();
		}
	}

	@Test
	@DisplayName("task가 예외를 던져도 finally에서 키가 제거되어 후속 요청이 통과한다")
	void execute_WhenTaskThrows_ShouldReleaseKey() {
		MeetingApplyInFlightGuard inFlightGuard = new MeetingApplyInFlightGuard();

		// 첫 요청이 처리 중 예외로 실패한다.
		assertThatThrownBy(() -> inFlightGuard.execute(1, 1, () -> {
			throw new IllegalStateException("boom");
		})).isInstanceOf(IllegalStateException.class);

		// finally에서 키가 제거됐으므로 같은 키가 다시 통과할 수 있다.
		assertThat(inFlightGuard.execute(1, 1, () -> "ok")).isEqualTo("ok");
	}

	private static void await(CountDownLatch latch) {
		try {
			latch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
	}
}
