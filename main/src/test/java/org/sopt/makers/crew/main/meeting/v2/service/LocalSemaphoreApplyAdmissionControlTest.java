package org.sopt.makers.crew.main.meeting.v2.service;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.global.config.ApplyAdmissionProperties;
import org.sopt.makers.crew.main.global.exception.ServerException;
import org.sopt.makers.crew.main.global.exception.ServiceUnavailableException;

@DisplayName("LocalSemaphoreApplyAdmissionControl 단위 테스트")
class LocalSemaphoreApplyAdmissionControlTest {

	@Test
	@DisplayName("permit을 획득하면 delegate를 실행한다")
	void execute_WhenPermitAcquired_ShouldInvokeDelegate() {
		AtomicBoolean invoked = new AtomicBoolean();
		LocalSemaphoreApplyAdmissionControl admissionControl = createAdmissionControl(1, Duration.ofMillis(100));

		String result = admissionControl.execute(() -> {
			invoked.set(true);
			return "success";
		});

		assertThat(invoked).isTrue();
		assertThat(result).isEqualTo("success");
	}

	@Test
	@DisplayName("permit이 모두 사용 중이면 다음 요청은 즉시 실패하지 않고 대기한다")
	void execute_WhenPermitUnavailable_ShouldWaitUntilReleased() throws Exception {
		LocalSemaphoreApplyAdmissionControl admissionControl = createAdmissionControl(1, Duration.ZERO);
		CountDownLatch entered = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		CountDownLatch secondStarted = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);

		try {
			Future<String> first = executor.submit(() -> admissionControl.execute(() -> {
				entered.countDown();
				await(release);
				return "first";
			}));
			assertThat(entered.await(1, TimeUnit.SECONDS)).isTrue();

			Future<String> second = executor.submit(() -> {
				secondStarted.countDown();
				return admissionControl.execute(() -> "second");
			});
			assertThat(secondStarted.await(1, TimeUnit.SECONDS)).isTrue();
			assertThatThrownBy(() -> second.get(50, TimeUnit.MILLISECONDS))
				.isInstanceOf(TimeoutException.class);

			release.countDown();
			assertThat(first.get(1, TimeUnit.SECONDS)).isEqualTo("first");
			assertThat(second.get(1, TimeUnit.SECONDS)).isEqualTo("second");
		} finally {
			release.countDown();
			executor.shutdownNow();
		}
	}

	@Test
	@DisplayName("설정된 timeout 안에 permit을 얻지 못하면 도메인 예외를 던진다")
	void execute_WhenPermitTimesOut_ShouldThrowDomainException() throws Exception {
		LocalSemaphoreApplyAdmissionControl admissionControl = createAdmissionControl(1, Duration.ofMillis(20));
		CountDownLatch entered = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		ExecutorService executor = Executors.newSingleThreadExecutor();

		try {
			Future<String> first = executor.submit(() -> admissionControl.execute(() -> {
				entered.countDown();
				await(release);
				return "first";
			}));
			assertThat(entered.await(1, TimeUnit.SECONDS)).isTrue();

			assertThatThrownBy(() -> admissionControl.execute(() -> "second"))
				.isInstanceOf(ServiceUnavailableException.class);

			release.countDown();
			assertThat(first.get(1, TimeUnit.SECONDS)).isEqualTo("first");
		} finally {
			release.countDown();
			executor.shutdownNow();
		}
	}

	@Test
	@DisplayName("delegate에서 예외가 발생해도 permit을 반환한다")
	void execute_WhenDelegateThrows_ShouldReleasePermit() {
		LocalSemaphoreApplyAdmissionControl admissionControl = createAdmissionControl(1, Duration.ZERO);

		assertThatThrownBy(() -> admissionControl.execute(() -> {
			throw new IllegalStateException("boom");
		})).isInstanceOf(IllegalStateException.class);

		assertThat(admissionControl.execute(() -> "next")).isEqualTo("next");
	}

	@Test
	@DisplayName("비활성화 상태에서는 permit을 획득하지 않고 delegate를 실행한다")
	void execute_WhenDisabled_ShouldBypassSemaphore() {
		LocalSemaphoreApplyAdmissionControl admissionControl = createAdmissionControl(false, 1, Duration.ofMillis(20));

		String result = admissionControl.execute(() -> admissionControl.execute(() -> "bypassed"));

		assertThat(result).isEqualTo("bypassed");
	}

	@Test
	@DisplayName("permit 대기 중 interrupt가 발생하면 flag를 복구하고 서버 예외를 던진다")
	void execute_WhenInterrupted_ShouldRestoreFlagAndThrowServerException() {
		LocalSemaphoreApplyAdmissionControl admissionControl = createAdmissionControl(1, Duration.ZERO);
		AtomicBoolean invoked = new AtomicBoolean();
		Thread.currentThread().interrupt();

		try {
			assertThatThrownBy(() -> admissionControl.execute(() -> {
				invoked.set(true);
				return "unexpected";
			})).isInstanceOf(ServerException.class);
			assertThat(Thread.currentThread().isInterrupted()).isTrue();
			assertThat(invoked).isFalse();
		} finally {
			Thread.interrupted();
		}
	}

	@Test
	@DisplayName("동시 실행 중인 delegate 수는 permit 수를 넘지 않는다")
	void execute_WhenRequestsAreConcurrent_ShouldLimitMaximumConcurrency() throws Exception {
		int permits = 2;
		int requestCount = 6;
		LocalSemaphoreApplyAdmissionControl admissionControl = createAdmissionControl(permits, Duration.ofSeconds(2));
		ExecutorService executor = Executors.newFixedThreadPool(requestCount);
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch firstBatchEntered = new CountDownLatch(permits);
		CountDownLatch release = new CountDownLatch(1);
		AtomicInteger current = new AtomicInteger();
		AtomicInteger maximum = new AtomicInteger();
		List<Future<Integer>> futures = new ArrayList<>();

		try {
			for (int i = 0; i < requestCount; i++) {
				futures.add(executor.submit(() -> {
					await(start);
					return admissionControl.execute(() -> {
						int concurrent = current.incrementAndGet();
						maximum.accumulateAndGet(concurrent, Math::max);
						firstBatchEntered.countDown();
						await(release);
						current.decrementAndGet();
						return concurrent;
					});
				}));
			}

			start.countDown();
			assertThat(firstBatchEntered.await(1, TimeUnit.SECONDS)).isTrue();
			assertThat(maximum).hasValueLessThanOrEqualTo(permits);
			release.countDown();

			for (Future<Integer> future : futures) {
				future.get(2, TimeUnit.SECONDS);
			}
			assertThat(maximum).hasValue(permits);
		} finally {
			release.countDown();
			executor.shutdownNow();
		}
	}

	private LocalSemaphoreApplyAdmissionControl createAdmissionControl(int permits, Duration waitTimeout) {
		return createAdmissionControl(true, permits, waitTimeout);
	}

	private LocalSemaphoreApplyAdmissionControl createAdmissionControl(boolean enabled, int permits,
		Duration waitTimeout) {
		ApplyAdmissionProperties properties = new ApplyAdmissionProperties(enabled, permits, waitTimeout);
		return new LocalSemaphoreApplyAdmissionControl(properties);
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
