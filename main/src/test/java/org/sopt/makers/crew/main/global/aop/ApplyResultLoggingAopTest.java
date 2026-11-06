package org.sopt.makers.crew.main.global.aop;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.sopt.makers.crew.main.entity.meeting.MeetingReader;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.ErrorStatus;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.sopt.makers.crew.main.global.exception.TooManyRequestsException;
import org.sopt.makers.crew.main.meeting.v2.dto.redis.MeetingCacheDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2ApplyMeetingDto;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@DisplayName("ApplyResultLoggingAop 단위 테스트")
class ApplyResultLoggingAopTest {

	private static final int MEETING_ID = 10;
	private static final int USER_ID = 7;
	private static final LocalDateTime OPEN_AT = LocalDateTime.of(2026, 11, 6, 12, 0, 0);
	private static final LocalDateTime REQUESTED_AT = OPEN_AT.plusNanos(350_000_000); // 오픈 0.35초 후

	private final MeetingReader meetingReader = mock(MeetingReader.class);
	private final ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
	private final ApplyResultLoggingAop aop = new ApplyResultLoggingAop(meetingReader, () -> REQUESTED_AT);

	private Logger logger;
	private ListAppender<ILoggingEvent> appender;

	@BeforeEach
	void setUp() {
		Principal principal = () -> String.valueOf(USER_ID);
		given(pjp.getArgs()).willReturn(new Object[] {new MeetingV2ApplyMeetingDto(MEETING_ID, "지원 동기"), principal});

		MeetingCacheDto meeting = mock(MeetingCacheDto.class);
		given(meeting.getStartDate()).willReturn(OPEN_AT);
		given(meetingReader.getMeetingById(MEETING_ID)).willReturn(meeting);

		logger = (Logger)LoggerFactory.getLogger(ApplyResultLoggingAop.class);
		appender = new ListAppender<>();
		appender.start();
		logger.addAppender(appender);
	}

	@AfterEach
	void tearDown() {
		logger.detachAppender(appender);
	}

	@Test
	@DisplayName("신청에 성공하면 결과, 모임·사용자, 오픈 후 경과 시간을 INFO로 남긴다")
	void logSuccess() throws Throwable {
		given(pjp.proceed()).willReturn("response");

		Object result = aop.logApplyResult(pjp, ApplyResultLoggingAop.TYPE_EVENT);

		assertThat(result).isEqualTo("response");
		ILoggingEvent event = singleEvent();
		assertThat(event.getLevel()).isEqualTo(Level.INFO);
		assertThat(event.getFormattedMessage())
			.contains("[APPLY_RESULT]", "type=EVENT", "result=SUCCESS", "meetingId=10", "userId=7", "status=201",
				"sinceOpenMs=350");
	}

	@Test
	@DisplayName("신청에 실패하면 HTTP 상태와 ErrorStatus 이름을 WARN으로 남기고 예외를 그대로 던진다")
	void logFailure() throws Throwable {
		BadRequestException exception = new BadRequestException(ErrorStatus.ALREADY_APPLIED_MEETING.getErrorCode());
		given(pjp.proceed()).willThrow(exception);

		assertThatThrownBy(() -> aop.logApplyResult(pjp, ApplyResultLoggingAop.TYPE_GENERAL)).isSameAs(exception);

		ILoggingEvent event = singleEvent();
		assertThat(event.getLevel()).isEqualTo(Level.WARN);
		assertThat(event.getFormattedMessage())
			.contains("type=GENERAL", "result=FAILURE", "status=400", "reason=ALREADY_APPLIED_MEETING");
	}

	@Test
	@DisplayName("컨트롤러 진입 후 거절(rate limit 등)도 상태 코드와 함께 남긴다")
	void logGuardRejection() throws Throwable {
		given(pjp.proceed()).willThrow(new TooManyRequestsException(ErrorStatus.TOO_MANY_REQUESTS.getErrorCode()));

		assertThatThrownBy(() -> aop.logApplyResult(pjp, ApplyResultLoggingAop.TYPE_EVENT))
			.isInstanceOf(TooManyRequestsException.class);

		assertThat(singleEvent().getFormattedMessage()).contains("status=429", "reason=TOO_MANY_REQUESTS");
	}

	@Test
	@DisplayName("모임 정보를 읽지 못해도 신청 결과에는 영향을 주지 않고 sinceOpenMs만 비워 둔다")
	void logWhenMeetingLookupFails() throws Throwable {
		given(meetingReader.getMeetingById(MEETING_ID)).willThrow(new NotFoundException());
		given(pjp.proceed()).willReturn("response");

		assertThat(aop.logApplyResult(pjp, ApplyResultLoggingAop.TYPE_EVENT)).isEqualTo("response");
		assertThat(singleEvent().getFormattedMessage()).contains("result=SUCCESS", "sinceOpenMs=null");
	}

	@Test
	@DisplayName("ErrorStatus에 없는 예외는 예외 클래스 이름과 500으로 남긴다")
	void reasonOfUnknownException() {
		IllegalStateException exception = new IllegalStateException("boom");

		assertThat(ApplyResultLoggingAop.reasonOf(exception)).isEqualTo("IllegalStateException");
		assertThat(ApplyResultLoggingAop.statusOf(exception)).isEqualTo(500);
	}

	private ILoggingEvent singleEvent() {
		List<ILoggingEvent> events = appender.list;
		assertThat(events).hasSize(1);
		return events.get(0);
	}
}
