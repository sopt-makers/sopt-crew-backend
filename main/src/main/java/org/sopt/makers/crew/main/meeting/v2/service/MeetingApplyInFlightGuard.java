package org.sopt.makers.crew.main.meeting.v2.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.sopt.makers.crew.main.global.annotation.NoExecutionLogging;
import org.sopt.makers.crew.main.global.exception.LockedException;
import org.springframework.stereotype.Component;

/**
 * 같은 사용자가 같은 모임에 동시에 보낸 신청 중 하나만 처리한다. (#895)
 *
 * <ul>
 *   <li>키는 (meetingId, userId)라 서로 다른 모임의 동시 신청은 막지 않는다.</li>
 *   <li>처리 중인 키로 들어온 요청은 기다리지 않고 즉시 423으로 거절해 요청 스레드를 붙잡지 않는다.</li>
 *   <li>task 안에서 신청 트랜잭션(REQUIRES_NEW)이 커밋된 뒤에 키를 해제하므로,
 *       후속 요청은 커밋된 신청을 보고 중복 검증에서 걸러진다.</li>
 * </ul>
 *
 * <p>한 JVM 안에서만 동작한다. 서버가 여러 대이거나 Lambda 인스턴스가 여러 개면 인스턴스끼리는 막지 못하고,
 * 최종 중복은 apply("meetingId", "userId") 유니크 인덱스가 막는다.</p>
 */
@Component
@NoExecutionLogging
public class MeetingApplyInFlightGuard {

	private final Set<ApplyKey> inFlight = ConcurrentHashMap.newKeySet();

	public <T> T execute(Integer meetingId, Integer userId, Supplier<T> task) {
		ApplyKey key = new ApplyKey(meetingId, userId);
		if (!inFlight.add(key)) {
			throw new LockedException(LOCK_ACQUISITION_TIMEOUT.getErrorCode());
		}
		try {
			return task.get();
		} finally {
			inFlight.remove(key);
		}
	}

	private record ApplyKey(Integer meetingId, Integer userId) {
	}
}
