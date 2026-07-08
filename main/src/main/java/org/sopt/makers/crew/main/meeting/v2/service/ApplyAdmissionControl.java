package org.sopt.makers.crew.main.meeting.v2.service;

import java.util.function.Supplier;

/**
 * 신청 핵심 구간의 동시 진입량을 제한한다.
 * 구현체를 교체해 분산 환경의 동기식 진입 제어로 확장할 수 있다.
 */
public interface ApplyAdmissionControl {

	<T> T execute(Supplier<T> task);
}
