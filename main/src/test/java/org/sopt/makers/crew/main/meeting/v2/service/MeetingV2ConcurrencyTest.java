package org.sopt.makers.crew.main.meeting.v2.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.ErrorStatus;
import org.sopt.makers.crew.main.global.exception.LockedException;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2ApplyMeetingDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2ApplyMeetingResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SqlGroup({
	@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
	@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
class MeetingV2ConcurrencyTest {
	@Autowired
	private MeetingV2Service meetingV2Service;

	@Autowired
	private MeetingRepository meetingRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ApplyRepository applyRepository;

	@Nested
	@DisplayName("모임 지원 락킹 테스트")
	class 모임_지원_락킹_테스트 {
		@Test
		@DisplayName("동일 사용자가 동시에 여러 신청을 시도할 경우 하나만 성공해야 한다")
		void applyMeetingWithLock_WhenSameUserRequestsConcurrently_ShouldProcessOnlyOne() throws InterruptedException {
			// given
			User leader = User.builder()
				.name("모임장")
				.orgId(1)
				.activities(List.of(new UserActivityVO("안드로이드", 35)))
				.profileImage("testProfileImage.jpg")
				.phone("010-1234-5678")
				.build();

			userRepository.save(leader);

			Meeting meeting = Meeting.builder()
				.user(leader)
				.userId(leader.getId())
				.title("모임 지원 테스트")
				.category(MeetingCategory.EVENT)
				.imageURL(List.of(new ImageUrlVO(0, "testImage.jpg")))
				.startDate(LocalDateTime.of(2024, 4, 23, 0, 0, 0))
				.endDate(LocalDateTime.of(2029, 4, 27, 23, 59, 59))
				.capacity(20)
				.desc("모임 지원 테스트입니다.")
				.processDesc("테스트 진행 방식입니다.")
				.mStartDate(LocalDateTime.of(2024, 11, 24, 0, 0, 0))
				.mEndDate(LocalDateTime.of(2024, 12, 24, 0, 0, 0))
				.leaderDesc("모임 리더 설명입니다.")
				.note("유의사항입니다.")
				.isMentorNeeded(false)
				.canJoinOnlyActiveGeneration(false)
				.createdGeneration(35)
				.targetActiveGeneration(null)
				.joinableParts(MeetingJoinablePart.values())
				.build();

			meetingRepository.save(meeting);

			User applicant = User.builder()
				.name("지원자")
				.orgId(2)
				.activities(List.of(new UserActivityVO("웹", 35)))
				.profileImage("applicantProfile.jpg")
				.phone("010-1234-5678")
				.build();

			userRepository.save(applicant);

			MeetingV2ApplyMeetingDto applyDto = new MeetingV2ApplyMeetingDto(meeting.getId(), "지원 동기");

			int concurrentRequests = 4;
			ExecutorService executorService = Executors.newFixedThreadPool(concurrentRequests);
			CountDownLatch startLatch = new CountDownLatch(1);
			CountDownLatch readyLatch = new CountDownLatch(concurrentRequests);
			CountDownLatch finishLatch = new CountDownLatch(concurrentRequests);

			AtomicInteger successCount = new AtomicInteger(0);
			AtomicInteger failCount = new AtomicInteger(0);
			List<Throwable> unexpectedErrors = Collections.synchronizedList(new ArrayList<>());

			// when
			for (int i = 0; i < concurrentRequests; i++) {
				executorService.submit(() -> {
					try {
						readyLatch.countDown();
						startLatch.await();

						MeetingV2ApplyMeetingResponseDto response = meetingV2Service.applyEventMeetingGuarded(applyDto,
							applicant.getId());

						if (response != null && response.getApplyId() != null) {
							successCount.incrementAndGet();
						}
					} catch (LockedException expected) {
						// 처리 중인 동일 요청은 sentinel이 즉시 거절한다.
						failCount.incrementAndGet();
					} catch (BadRequestException expected) {
						// 앞선 요청이 커밋된 뒤라면 validator의 중복 검증 또는 DB unique 위반이
						// ALREADY_APPLIED_MEETING으로 변환된다. 그 외 검증 오류는 예상 밖이므로 별도로 수집한다.
						if (ErrorStatus.ALREADY_APPLIED_MEETING.getErrorCode().equals(expected.getErrorCode())) {
							failCount.incrementAndGet();
						} else {
							unexpectedErrors.add(expected);
						}
					} catch (Throwable unexpected) {
						unexpectedErrors.add(unexpected);
					} finally {
						finishLatch.countDown();
					}
				});
			}

			boolean completedInTime;
			try {
				boolean readyInTime = readyLatch.await(5, TimeUnit.SECONDS);
				assertThat(readyInTime).isTrue();

				startLatch.countDown();
				completedInTime = finishLatch.await(10, TimeUnit.SECONDS);
			} finally {
				// 준비 단계에서 실패해도 대기 스레드를 풀고 executor가 완전히 종료될 때까지 기다린다.
				startLatch.countDown();
				executorService.shutdownNow();
				executorService.awaitTermination(2, TimeUnit.SECONDS);
			}

			// then
			Assertions.assertThat(unexpectedErrors)
				.as("예상하지 못한 예외가 발생하면 안 된다")
				.isEmpty();
			assertThat(completedInTime).isTrue();
			assertThat(successCount.get()).isOne();
			assertThat(failCount.get()).isEqualTo(concurrentRequests - 1);

			List<Apply> allApplies = applyRepository.findAllByMeetingId(meeting.getId());
			List<Apply> userApplies = allApplies.stream()
				.filter(apply -> apply.getUserId().equals(applicant.getId()))
				.toList();

			Assertions.assertThat(userApplies).hasSize(1);
		}

		@Test
		@DisplayName("서로 다른 사용자가 동시에 신청하는 경우 모두 성공해야 한다")
		void applyMeetingWithLock_WhenMultipleRequestsFromDifferentUsers_ShouldProcessAll() throws
			InterruptedException {
			// given
			User leader = User.builder()
				.name("모임장")
				.orgId(1)
				.activities(List.of(new UserActivityVO("안드로이드", 35)))
				.profileImage("testProfileImage.jpg")
				.phone("010-1234-5678")
				.build();

			userRepository.save(leader);

			Meeting meeting = Meeting.builder()
				.user(leader)
				.userId(leader.getId())
				.title("락킹 테스트 모임")
				.category(MeetingCategory.EVENT)
				.imageURL(List.of(new ImageUrlVO(0, "testImage.jpg")))
				.startDate(LocalDateTime.of(2024, 4, 23, 0, 0, 0))
				.endDate(LocalDateTime.of(2029, 4, 27, 23, 59, 59))
				.capacity(20)
				.desc("모임 지원 테스트입니다.")
				.processDesc("테스트 진행 방식입니다.")
				.mStartDate(LocalDateTime.of(2024, 11, 24, 0, 0, 0))
				.mEndDate(LocalDateTime.of(2024, 12, 24, 0, 0, 0))
				.leaderDesc("모임 리더 설명입니다.")
				.note("유의사항입니다.")
				.isMentorNeeded(false)
				.canJoinOnlyActiveGeneration(false)
				.createdGeneration(35)
				.targetActiveGeneration(null)
				.joinableParts(MeetingJoinablePart.values())
				.build();

			meetingRepository.save(meeting);

			int userCount = 5;
			List<User> applicants = new ArrayList<>();

			for (int i = 0; i < userCount; i++) {
				User applicant = User.builder()
					.name("지원자" + i)
					.orgId(100 + i)
					.activities(List.of(new UserActivityVO("웹", 35)))
					.profileImage("applicantProfile" + i + ".jpg")
					.phone("010-1234-" + (5678 + i))
					.build();

				applicants.add(userRepository.save(applicant));
			}

			ExecutorService executorService = Executors.newFixedThreadPool(userCount);
			CountDownLatch startLatch = new CountDownLatch(1);
			CountDownLatch readyLatch = new CountDownLatch(userCount);
			CountDownLatch finishLatch = new CountDownLatch(userCount);

			AtomicInteger successCount = new AtomicInteger(0);
			List<Integer> applyIds = Collections.synchronizedList(new ArrayList<>());
			List<Throwable> unexpectedErrors = Collections.synchronizedList(new ArrayList<>());

			// when
			for (int i = 0; i < userCount; i++) {
				final int index = i;
				executorService.submit(() -> {
					try {
						readyLatch.countDown();
						startLatch.await();

						User applicant = applicants.get(index);
						MeetingV2ApplyMeetingDto applyDto = new MeetingV2ApplyMeetingDto(meeting.getId(),
							"지원 동기 " + index);

						MeetingV2ApplyMeetingResponseDto response = meetingV2Service.applyEventMeetingGuarded(applyDto,
							applicant.getId());

						if (response != null && response.getApplyId() != null) {
							successCount.incrementAndGet();
							applyIds.add(response.getApplyId());
						}
					} catch (Throwable unexpected) {
						// 서로 다른 사용자의 신청은 서로를 막지 않으므로 어떤 예외도 발생하면 안 된다.
						unexpectedErrors.add(unexpected);
					} finally {
						finishLatch.countDown();
					}
				});
			}

			boolean completedInTime;
			try {
				boolean readyInTime = readyLatch.await(5, TimeUnit.SECONDS);
				assertThat(readyInTime).isTrue();

				startLatch.countDown();
				completedInTime = finishLatch.await(10, TimeUnit.SECONDS);
			} finally {
				startLatch.countDown();
				executorService.shutdownNow();
				executorService.awaitTermination(2, TimeUnit.SECONDS);
			}

			// then
			Assertions.assertThat(unexpectedErrors)
				.as("서로 다른 사용자의 동시 신청은 모두 성공해야 한다")
				.isEmpty();
			assertThat(completedInTime).isTrue();
			assertThat(successCount.get()).isEqualTo(userCount);
			Assertions.assertThat(applyIds)
				.doesNotHaveDuplicates()
				.hasSize(userCount);

			for (User applicant : applicants) {
				boolean exists = applyRepository.existsByMeetingIdAndUserId(meeting.getId(), applicant.getId());
				assertThat(exists).isTrue();
			}

			List<Apply> allApplies = applyRepository.findAllByMeetingId(meeting.getId());
			Assertions.assertThat(allApplies).hasSize(userCount);

			Set<Integer> userIds = allApplies.stream()
				.map(Apply::getUserId)
				.collect(Collectors.toSet());
			Assertions.assertThat(userIds).hasSize(userCount);
		}

		@Test
		@DisplayName("작업 완료 후 sentinel이 해제되어 후속 요청이 비즈니스 검증까지 진입한다")
		void applyMeetingWithLock_WhenSentinelReleasedAfterCommit_ShouldReachBusinessValidation() {
			// given
			User leader = userRepository.save(User.builder()
				.name("모임장")
				.orgId(1)
				.activities(List.of(new UserActivityVO("안드로이드", 35)))
				.profileImage("testProfileImage.jpg")
				.phone("010-1234-5678")
				.build());

			Meeting meeting = meetingRepository.save(Meeting.builder()
				.user(leader)
				.userId(leader.getId())
				.title("락킹 테스트 모임")
				.category(MeetingCategory.EVENT)
				.imageURL(List.of(new ImageUrlVO(0, "testImage.jpg")))
				.startDate(LocalDateTime.of(2024, 4, 23, 0, 0, 0))
				.endDate(LocalDateTime.of(2029, 4, 27, 23, 59, 59))
				.capacity(20)
				.desc("모임 지원 테스트입니다.")
				.processDesc("테스트 진행 방식입니다.")
				.mStartDate(LocalDateTime.of(2024, 11, 24, 0, 0, 0))
				.mEndDate(LocalDateTime.of(2024, 12, 24, 0, 0, 0))
				.leaderDesc("모임 리더 설명입니다.")
				.note("유의사항입니다.")
				.isMentorNeeded(false)
				.canJoinOnlyActiveGeneration(false)
				.createdGeneration(35)
				.targetActiveGeneration(null)
				.joinableParts(MeetingJoinablePart.values())
				.build());

			User applicant = userRepository.save(User.builder()
				.name("지원자")
				.orgId(2)
				.activities(List.of(new UserActivityVO("웹", 35)))
				.profileImage("applicantProfile.jpg")
				.phone("010-1234-5678")
				.build());

			MeetingV2ApplyMeetingDto applyDto = new MeetingV2ApplyMeetingDto(meeting.getId(), "지원 동기");

			// when: 첫 신청은 정상적으로 저장되고, 트랜잭션이 커밋되면서 sentinel도 해제된다.
			MeetingV2ApplyMeetingResponseDto firstResponse =
				meetingV2Service.applyEventMeetingGuarded(applyDto, applicant.getId());
			assertThat(firstResponse.getApplyId()).isNotNull();

			List<Apply> applies = applyRepository.findAllByMeetingId(meeting.getId());
			Assertions.assertThat(applies).hasSize(1);

			// then: sentinel이 해제됐으므로 후속 요청은 비즈니스 검증까지 진입해 중복으로 거절된다.
			assertThatThrownBy(() -> meetingV2Service.applyEventMeetingGuarded(applyDto, applicant.getId()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("이미 지원한 모임입니다");

			applies = applyRepository.findAllByMeetingId(meeting.getId());
			Assertions.assertThat(applies).hasSize(1);
		}
	}
}
