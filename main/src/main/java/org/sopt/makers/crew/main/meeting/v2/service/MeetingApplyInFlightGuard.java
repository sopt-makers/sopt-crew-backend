package org.sopt.makers.crew.main.meeting.v2.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.sopt.makers.crew.main.global.exception.LockedException;
import org.springframework.stereotype.Component;

@Component
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
