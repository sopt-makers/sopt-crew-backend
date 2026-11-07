package org.sopt.makers.crew.main.dining.v2.service;

import static org.assertj.core.api.Assertions.*;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateDiningBodyDto;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateRoundBodyDto;
import org.sopt.makers.crew.main.entity.dining.DiningAttendanceRepository;
import org.sopt.makers.crew.main.entity.dining.DiningDeletionHistoryRepository;
import org.sopt.makers.crew.main.entity.dining.DiningRound;
import org.sopt.makers.crew.main.entity.dining.DiningRoundRepository;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.external.CaffeineTestConfig;
import org.sopt.makers.crew.main.global.annotation.IntegratedTest;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * 회차·회식 삭제와 참석 추가(INSERT)의 경합.
 * 참석 행 잠금만으로는 새로 들어오는 INSERT를 막지 못해, 삭제 중 커밋된 참석이 이력 없이 CASCADE 삭제될 수 있었다.
 */
@IntegratedTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(CaffeineTestConfig.class)
class DiningRoundDeletionRaceTest {

	@Autowired
	private DiningV2Service diningV2Service;
	@Autowired
	private DiningAttendanceRepository attendanceRepository;
	@Autowired
	private DiningRoundRepository roundRepository;
	@Autowired
	private DiningDeletionHistoryRepository historyRepository;
	@Autowired
	private MeetingRepository meetingRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	@Qualifier("primaryTransactionManager")
	private PlatformTransactionManager transactionManager;
	@Autowired
	@Qualifier("primaryDatasourceProperties")
	private DataSource dataSource;
	@PersistenceContext(unitName = "primaryEntityManagerFactory")
	private EntityManager entityManager;

	// 실제 DB와 서비스는 그대로 사용하고 외부 알림만 차단한다.
	@MockBean
	private DiningNotificationSender notificationSender;

	private TransactionTemplate transaction;
	private JdbcTemplate jdbc;
	private Fixture fixture;

	@BeforeEach
	void setUp() {
		transaction = new TransactionTemplate(transactionManager);
		transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		transaction.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
		jdbc = new JdbcTemplate(dataSource);

		// 작업 스레드의 독립 트랜잭션에서 조회할 수 있도록 준비 데이터를 먼저 커밋한다.
		fixture = transaction.execute(status -> {
			User leader = userRepository.save(user("개설자", 94201));
			User attendee = userRepository.save(user("기존 참석자", 94202));
			User newcomer = userRepository.save(user("신규 참석자", 94203));
			Integer meetingId = diningV2Service.createDining(new DiningV2CreateAndUpdateDiningBodyDto(
				"삭제 참석 경합 회식", "1차", null, null, "2024.04.30", "18:00",
				new MeetingJoinablePart[] {MeetingJoinablePart.SERVER}, List.of("https://image.test/dining.png")),
				leader.getId()).meetingId();
			Integer roundId = diningV2Service.createRound(meetingId,
				new DiningV2CreateAndUpdateRoundBodyDto("2차", null, null, "20:00"), leader.getId()).roundId();
			diningV2Service.applyAttendance(meetingId, roundId, attendee.getId());
			return new Fixture(meetingId, roundId, leader.getId(), attendee.getId(), newcomer.getId(),
				roundRepository.findAllByMeetingIdOrderByIdAsc(meetingId).stream().map(DiningRound::getId).toList());
		});
	}

	@AfterEach
	void cleanUp() {
		if (fixture == null) {
			return;
		}
		transaction.executeWithoutResult(status -> {
			historyRepository.deleteAllInBatch(historyRepository.findAll().stream()
				.filter(history -> history.getMeetingId().equals(fixture.meetingId())).toList());
			attendanceRepository.deleteAllInBatch(attendanceRepository.findAllByRoundIdIn(fixture.roundIds()));
			roundRepository.deleteAllInBatch(roundRepository.findAllById(fixture.roundIds()));
			meetingRepository.findById(fixture.meetingId()).ifPresent(meetingRepository::delete);
			meetingRepository.flush();
			userRepository.findAllById(List.of(fixture.leaderId(), fixture.attendeeId(), fixture.newcomerId()))
				.forEach(entityManager::remove);
		});
	}

	@ParameterizedTest(name = "{0}")
	@EnumSource(Deletion.class)
	@DisplayName("커밋 전 참석 추가가 있으면 삭제는 그 커밋을 기다린 뒤 새 참석까지 이력에 보관한다")
	void deletionWaitsForPendingAttendanceAndKeepsItInHistory(Deletion deletion) throws Exception {
		ExecutorService workers = Executors.newFixedThreadPool(2);
		CountDownLatch finishAttendance = new CountDownLatch(1);
		CompletableFuture<Integer> attendanceReady = new CompletableFuture<>();
		CompletableFuture<Integer> deletionStarted = new CompletableFuture<>();
		try {
			var attendance = workers.submit(() -> transaction.executeWithoutResult(status -> {
				int pid = backendPid();
				diningV2Service.applyAttendance(fixture.meetingId(), fixture.roundId(), fixture.newcomerId());
				attendanceReady.complete(pid);
				awaitRelease(finishAttendance);
			}));
			int attendancePid = attendanceReady.get(10, TimeUnit.SECONDS);
			var removal = workers.submit(() -> transaction.executeWithoutResult(status -> {
				deletionStarted.complete(backendPid());
				delete(deletion);
				entityManager.flush();
			}));
			int deletionPid = deletionStarted.get(10, TimeUnit.SECONDS);

			awaitBlockedBy(deletionPid, attendancePid);
			assertThat(removal.isDone()).isFalse();
			finishAttendance.countDown();
			attendance.get(10, TimeUnit.SECONDS);
			removal.get(10, TimeUnit.SECONDS);

			transaction.executeWithoutResult(status -> {
				assertThat(attendanceRepository.findAllByRoundId(fixture.roundId())).isEmpty();
				// 개설자는 회차 생성 시 자동 참석한다.
				assertThat(snapshotUserIds(deletion))
					.containsExactlyInAnyOrder(fixture.leaderId(), fixture.attendeeId(), fixture.newcomerId());
			});
		} finally {
			finishAttendance.countDown();
			shutdown(workers);
		}
	}

	@ParameterizedTest(name = "{0}")
	@EnumSource(Deletion.class)
	@DisplayName("삭제가 먼저 회차를 잠그면 이후 참석 추가는 삭제 커밋 후 404로 실패한다")
	void attendanceAfterDeletionLockFailsWithNotFound(Deletion deletion) throws Exception {
		ExecutorService workers = Executors.newFixedThreadPool(2);
		CountDownLatch finishDeletion = new CountDownLatch(1);
		CompletableFuture<Integer> deletionReady = new CompletableFuture<>();
		CompletableFuture<Integer> attendanceStarted = new CompletableFuture<>();
		try {
			var removal = workers.submit(() -> transaction.executeWithoutResult(status -> {
				int pid = backendPid();
				delete(deletion);
				entityManager.flush();
				deletionReady.complete(pid);
				awaitRelease(finishDeletion);
			}));
			int deletionPid = deletionReady.get(10, TimeUnit.SECONDS);
			var attendance = workers.submit(() -> transaction.executeWithoutResult(status -> {
				attendanceStarted.complete(backendPid());
				diningV2Service.applyAttendance(fixture.meetingId(), fixture.roundId(), fixture.newcomerId());
			}));
			int attendancePid = attendanceStarted.get(10, TimeUnit.SECONDS);

			awaitBlockedBy(attendancePid, deletionPid);
			finishDeletion.countDown();
			removal.get(10, TimeUnit.SECONDS);

			assertThatThrownBy(() -> attendance.get(10, TimeUnit.SECONDS))
				.isInstanceOf(ExecutionException.class)
				.cause()
				.isInstanceOf(NotFoundException.class)
				.hasMessage(NOT_FOUND_DINING_ROUND.getErrorCode());
			transaction.executeWithoutResult(status -> {
				assertThat(attendanceRepository.findAllByRoundId(fixture.roundId())).isEmpty();
				assertThat(snapshotUserIds(deletion)).containsExactlyInAnyOrder(fixture.leaderId(), fixture.attendeeId());
			});
		} finally {
			finishDeletion.countDown();
			shutdown(workers);
		}
	}

	private void delete(Deletion deletion) {
		switch (deletion) {
			case ROUND -> diningV2Service.deleteRound(fixture.meetingId(), fixture.roundId(), fixture.leaderId());
			case DINING -> diningV2Service.deleteDining(fixture.meetingId(), fixture.leaderId());
		}
	}

	/** 삭제 이력 스냅샷에서 대상 회차(2차)의 참석자 userId 목록 */
	@SuppressWarnings("unchecked")
	private List<Integer> snapshotUserIds(Deletion deletion) {
		var histories = historyRepository.findAll().stream()
			.filter(history -> history.getMeetingId().equals(fixture.meetingId())).toList();
		assertThat(histories).hasSize(1);
		Map<String, Object> data = (Map<String, Object>)histories.get(0).getSnapshot().get("data");
		if (deletion == Deletion.DINING) {
			data = ((List<Map<String, Object>>)data.get("rounds")).stream()
				.filter(round -> fixture.roundId().equals(round.get("roundId"))).findFirst().orElseThrow();
		}
		return ((List<Map<String, Object>>)data.get("attendances")).stream()
			.map(attendance -> (Integer)attendance.get("userId")).toList();
	}

	private void awaitBlockedBy(int waitingPid, int blockingPid) throws InterruptedException {
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
		while (System.nanoTime() < deadline) {
			if (Boolean.TRUE.equals(jdbc.queryForObject(
				"SELECT ? = ANY(pg_blocking_pids(?))", Boolean.class, blockingPid, waitingPid))) {
				return;
			}
			TimeUnit.MILLISECONDS.sleep(20);
		}
		fail("대기해야 할 트랜잭션이 상대 트랜잭션의 잠금을 기다리지 않았습니다.");
	}

	private int backendPid() {
		entityManager.createNativeQuery("SET LOCAL lock_timeout = '20s'").executeUpdate();
		return ((Number)entityManager.createNativeQuery("SELECT pg_backend_pid()").getSingleResult()).intValue();
	}

	private static void awaitRelease(CountDownLatch latch) {
		try {
			if (!latch.await(30, TimeUnit.SECONDS)) {
				throw new AssertionError("트랜잭션 종료 신호를 받지 못했습니다.");
			}
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new AssertionError(exception);
		}
	}

	private static void shutdown(ExecutorService workers) throws InterruptedException {
		workers.shutdown();
		if (!workers.awaitTermination(15, TimeUnit.SECONDS)) {
			workers.shutdownNow();
			assertThat(workers.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
		}
	}

	private User user(String name, Integer orgId) {
		return User.builder().name(name).orgId(orgId).activities(List.of(new UserActivityVO("서버", 35)))
			.profileImage("profile.jpg").phone("010-1234-5678").build();
	}

	private record Fixture(Integer meetingId, Integer roundId, Integer leaderId, Integer attendeeId,
		Integer newcomerId, List<Integer> roundIds) {
	}

	private enum Deletion {
		ROUND, DINING
	}
}
