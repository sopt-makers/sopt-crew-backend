package org.sopt.makers.crew.main.user.v2.service.lock;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.sopt.makers.crew.main.global.exception.LockedException;
import org.sopt.makers.crew.main.global.exception.ServerException;
import org.springframework.stereotype.Component;

@Component
public class UserLockManager {
	private static final long DEFAULT_TIMEOUT_MS = 5000;
	private final Map<Integer, LockWrapper> userLocks = new ConcurrentHashMap<>();

	/**
	 * 사용자 ID에 대한 락을 획득하고, 작업을 실행한 후 락을 해제합니다.
	 * 타임아웃은 5초로 설정됩니다.
	 *
	 * @param userId 락을 획득할 사용자 ID
	 * @param task 락 내에서 실행할 작업
	 * @return 작업 실행 결과
	 * @throws RuntimeException 락 획득 실패 또는 작업 실행 중 예외 발생 시
	 */
	public <T> T executeWithLock(Integer userId, Supplier<T> task) {
		LockWrapper wrapper = getLockWrapper(userId);
		boolean locked = false;

		try {
			wrapper.usageCount.incrementAndGet();
			locked = wrapper.lock.tryLock(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);

			if (!locked) {
				throw new LockedException(LOCK_ACQUISITION_TIMEOUT.getErrorCode());
			}

			return task.get();

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ServerException(INTERNAL_SERVER_ERROR.getErrorCode());
		} finally {
			if (locked) {
				wrapper.lock.unlock();
			}

			if (wrapper.usageCount.decrementAndGet() == 0) {
				userLocks.remove(userId, wrapper);
			}
		}
	}

	private LockWrapper getLockWrapper(Integer userId) {
		return userLocks.computeIfAbsent(userId, id -> new LockWrapper());
	}

	private static class LockWrapper {
		final ReentrantLock lock = new ReentrantLock();
		final AtomicInteger usageCount = new AtomicInteger(0);
	}
}
