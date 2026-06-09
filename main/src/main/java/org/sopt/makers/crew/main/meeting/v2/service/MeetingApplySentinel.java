package org.sopt.makers.crew.main.meeting.v2.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.sopt.makers.crew.main.global.exception.LockedException;
import org.springframework.stereotype.Component;

@Component
public class MeetingApplySentinel {

	private final Set<Integer> inFlight = ConcurrentHashMap.newKeySet();

	public <T> T guard(Integer userId, Supplier<T> action) {
		if (!inFlight.add(userId)) {
			throw new LockedException(LOCK_ACQUISITION_TIMEOUT.getErrorCode());
		}
		try {
			return action.get();
		} finally {
			inFlight.remove(userId);
		}
	}
}
