package org.sopt.makers.crew.main.dining.v2.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateDiningBodyDto;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateRoundBodyDto;
import org.sopt.makers.crew.main.entity.dining.DiningAttendanceRepository;
import org.sopt.makers.crew.main.entity.dining.DiningDeletionHistoryRepository;
import org.sopt.makers.crew.main.entity.dining.DiningRound;
import org.sopt.makers.crew.main.entity.dining.DiningRoundRepository;
import org.sopt.makers.crew.main.entity.dining.enums.DiningDeletionTargetType;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.external.CaffeineTestConfig;
import org.sopt.makers.crew.main.global.annotation.IntegratedTest;
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

@IntegratedTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(CaffeineTestConfig.class)
class DiningDeletionConcurrencyTest {

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
		assertThat(jdbc.queryForObject("SELECT version()", String.class)).contains("PostgreSQL");

		// 작업 스레드의 독립 트랜잭션에서 조회할 수 있도록 준비 데이터를 먼저 커밋한다.
		fixture = transaction.execute(status -> {
			User leader = userRepository.save(user("개설자", 93701));
			User requester = userRepository.save(user("결제자", 93702));
			Integer meetingId = diningV2Service.createDining(new DiningV2CreateAndUpdateDiningBodyDto(
				"삭제 경합 회식", "1차", null, null, "2024.04.30", "18:00",
				new MeetingJoinablePart[] {MeetingJoinablePart.SERVER}, List.of("https://image.test/dining.png")),
				leader.getId()).meetingId();
			Integer roundId = diningV2Service.createRound(meetingId,
				new DiningV2CreateAndUpdateRoundBodyDto("2차", null, null, "20:00"), leader.getId()).roundId();
			diningV2Service.applyAttendance(meetingId, roundId, requester.getId());
			diningV2Service.requestPayer(meetingId, roundId, requester.getId());
			var attendance = attendanceRepository.findByRoundIdAndUserId(roundId, requester.getId()).orElseThrow();
			return new Fixture(meetingId, roundId, attendance.getId(), leader.getId(), requester.getId(),
				attendance.getPayerRequestedAt(), roundRepository.findAllByMeetingIdOrderByIdAsc(meetingId).stream()
					.map(DiningRound::getId).toList());
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
			userRepository.findAllById(List.of(fixture.leaderId(), fixture.requesterId())).forEach(entityManager::remove);
		});
	}

	@ParameterizedTest(name = "{0}, 승인 롤백={1}")
	@CsvSource({
		"CANCEL,false", "ATTENDEE,false", "ROUND,false", "DINING,false",
		"CANCEL,true", "ATTENDEE,true", "ROUND,true", "DINING,true"
	})
	@DisplayName("삭제는 진행 중인 승인을 기다린 뒤 마지막 커밋 상태를 이력에 보관한다")
	void deletionSnapshotsStateAfterApprovalTransactionFinishes(Deletion deletion, boolean rollback) throws Exception {
		ExecutorService workers = Executors.newFixedThreadPool(2);
		CountDownLatch finishApproval = new CountDownLatch(1);
		CompletableFuture<Integer> approvalReady = new CompletableFuture<>();
		CompletableFuture<Integer> deletionStarted = new CompletableFuture<>();
		try {
			var approval = workers.submit(() -> transaction.executeWithoutResult(status -> {
				int pid = backendPid();
				diningV2Service.approvePayer(fixture.meetingId(), fixture.roundId(), fixture.requesterId(), fixture.leaderId());
				entityManager.flush();
				approvalReady.complete(pid);
				awaitRelease(finishApproval);
				if (rollback) {
					status.setRollbackOnly();
				}
			}));
			int approvalPid = approvalReady.get(10, TimeUnit.SECONDS);
			var removal = workers.submit(() -> transaction.executeWithoutResult(status -> {
				deletionStarted.complete(backendPid());
				delete(deletion);
				entityManager.flush();
			}));
			int deletionPid = deletionStarted.get(10, TimeUnit.SECONDS);
			assertThat(deletionPid).isNotEqualTo(approvalPid);

			// 시간 지연을 추측하지 않고 PostgreSQL이 실제로 승인 트랜잭션을 기다리는지 확인한다.
			awaitBlockedBy(deletionPid, approvalPid);
			assertThat(removal.isDone()).isFalse();
			finishApproval.countDown();
			approval.get(10, TimeUnit.SECONDS);
			removal.get(10, TimeUnit.SECONDS);

			transaction.executeWithoutResult(status -> {
				assertThat(attendanceRepository.findById(fixture.attendanceId())).isEmpty();
				var histories = historyRepository.findAll().stream()
					.filter(history -> history.getMeetingId().equals(fixture.meetingId())).toList();
				assertThat(histories).hasSize(1);
				var history = histories.get(0);
				assertThat(history.getTargetType()).isEqualTo(deletion.targetType);
				assertThat(history.getTargetId()).isEqualTo(switch (deletion) {
					case CANCEL, ATTENDEE -> fixture.attendanceId();
					case ROUND -> fixture.roundId();
					case DINING -> fixture.meetingId();
				});
				assertThat(history.getDeletedByUserId()).isEqualTo(
					deletion == Deletion.CANCEL ? fixture.requesterId() : fixture.leaderId());
				Map<String, Object> snapshot = attendanceSnapshot(history.getSnapshot(), deletion);
				assertThat(snapshot.get("payerStatus")).isEqualTo(rollback ? "PENDING" : "APPROVED");
				assertThat(snapshot.get("payerRequestedAt")).isEqualTo(fixture.requestedAt().toString());
			});
		} finally {
			finishApproval.countDown();
			workers.shutdown();
			if (!workers.awaitTermination(15, TimeUnit.SECONDS)) {
				workers.shutdownNow();
				assertThat(workers.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
			}
		}
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
		fail("삭제 트랜잭션이 승인 트랜잭션의 잠금을 기다리지 않았습니다.");
	}

	private int backendPid() {
		entityManager.createNativeQuery("SET LOCAL lock_timeout = '20s'").executeUpdate();
		return ((Number)entityManager.createNativeQuery("SELECT pg_backend_pid()").getSingleResult()).intValue();
	}

	private void delete(Deletion deletion) {
		switch (deletion) {
			case CANCEL -> diningV2Service.cancelAttendance(fixture.meetingId(), fixture.roundId(), fixture.requesterId());
			case ATTENDEE -> diningV2Service.deleteAttendee(fixture.meetingId(), fixture.roundId(), fixture.requesterId(), fixture.leaderId());
			case ROUND -> diningV2Service.deleteRound(fixture.meetingId(), fixture.roundId(), fixture.leaderId());
			case DINING -> diningV2Service.deleteDining(fixture.meetingId(), fixture.leaderId());
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> attendanceSnapshot(Map<String, Object> snapshot, Deletion deletion) {
		Map<String, Object> data = (Map<String, Object>)snapshot.get("data");
		if (deletion == Deletion.DINING) {
			data = ((List<Map<String, Object>>)data.get("rounds")).stream()
				.filter(round -> fixture.roundId().equals(round.get("roundId"))).findFirst().orElseThrow();
		}
		if (deletion == Deletion.DINING || deletion == Deletion.ROUND) {
			return ((List<Map<String, Object>>)data.get("attendances")).stream()
				.filter(attendance -> fixture.attendanceId().equals(attendance.get("attendanceId")))
				.findFirst().orElseThrow();
		}
		return data;
	}

	private static void awaitRelease(CountDownLatch latch) {
		try {
			if (!latch.await(30, TimeUnit.SECONDS)) {
				throw new AssertionError("승인 트랜잭션 종료 신호를 받지 못했습니다.");
			}
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new AssertionError(exception);
		}
	}

	private User user(String name, Integer orgId) {
		return User.builder().name(name).orgId(orgId).activities(List.of(new UserActivityVO("서버", 35)))
			.profileImage("profile.jpg").phone("010-1234-5678").build();
	}

	private record Fixture(Integer meetingId, Integer roundId, Integer attendanceId, Integer leaderId,
		Integer requesterId, LocalDateTime requestedAt, List<Integer> roundIds) {
	}

	private enum Deletion {
		CANCEL(DiningDeletionTargetType.ATTENDANCE),
		ATTENDEE(DiningDeletionTargetType.ATTENDANCE),
		ROUND(DiningDeletionTargetType.ROUND),
		DINING(DiningDeletionTargetType.DINING);

		private final DiningDeletionTargetType targetType;

		Deletion(DiningDeletionTargetType targetType) {
			this.targetType = targetType;
		}
	}
}
