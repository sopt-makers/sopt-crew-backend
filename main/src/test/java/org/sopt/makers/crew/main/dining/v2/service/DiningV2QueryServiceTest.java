package org.sopt.makers.crew.main.dining.v2.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.dining.v2.dto.query.DiningV2GetDiningsQueryDto;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateDiningBodyDto;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateRoundBodyDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetDiningDetailResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetDiningDetailResponseDto.MyAttendanceStatus;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetDiningsResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetDiningsResponseDto.DiningCard;
import org.sopt.makers.crew.main.entity.dining.DiningRound;
import org.sopt.makers.crew.main.entity.dining.DiningRoundRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.external.CaffeineTestConfig;
import org.sopt.makers.crew.main.global.annotation.IntegratedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * 회식 목록, 상세, 자동 확정 테스트. StubTime 기준 현재 시각: 2024.04.24 00:00
 */
@IntegratedTest
@Import(CaffeineTestConfig.class)
class DiningV2QueryServiceTest {

	@Autowired
	private DiningV2Service diningV2Service;
	@Autowired
	private DiningRoundRepository diningRoundRepository;
	@Autowired
	private UserRepository userRepository;

	private User leader;
	private User member;

	@BeforeEach
	void setUp() {
		leader = userRepository.save(createUser("개설자", 1));
		member = userRepository.save(createUser("참석자", 2));
	}

	private User createUser(String name, Integer orgId) {
		return User.builder()
			.name(name)
			.orgId(orgId)
			.activities(List.of(new UserActivityVO("서버", 35)))
			.profileImage("profile.jpg")
			.phone("010-1234-5678")
			.build();
	}

	private Integer createDining(User owner, String title, String diningDate, MeetingJoinablePart... parts) {
		return diningV2Service.createDining(new DiningV2CreateAndUpdateDiningBodyDto(
			title, null, null, null, diningDate, null, parts, null), owner.getId()).meetingId();
	}

	private Integer createRound(Integer meetingId) {
		return diningV2Service.createRound(meetingId, new DiningV2CreateAndUpdateRoundBodyDto(null, null, null, null),
			leader.getId()).roundId();
	}

	private Integer firstRoundId(Integer meetingId) {
		return diningRoundRepository.findAllByMeetingIdOrderByIdAsc(meetingId).get(0).getId();
	}

	private DiningV2GetDiningsQueryDto query(String tab, String status, MeetingJoinablePart part, String keyword) {
		return new DiningV2GetDiningsQueryDto(null, null, tab, status, part, keyword);
	}

	private List<Integer> meetingIds(DiningV2GetDiningsResponseDto response) {
		return response.dinings().stream().map(DiningCard::meetingId).toList();
	}

	@Nested
	class 메인_목록 {

		@Test
		@DisplayName("페이지 크기 기준으로 중복이나 누락 없이 페이지가 나뉜다.")
		void getDinings_paginatesWithoutDuplicateOrMissing() {
			// given: 정렬 순서 5월 20일 → 5월 10일 → 4월 30일
			Integer latest = createDining(leader, "5월 20일 회식", "2024.05.20", MeetingJoinablePart.SERVER);
			Integer middle = createDining(leader, "5월 10일 회식", "2024.05.10", MeetingJoinablePart.SERVER);
			Integer oldest = createDining(leader, "4월 30일 회식", "2024.04.30", MeetingJoinablePart.SERVER);

			// when
			DiningV2GetDiningsResponseDto firstPage = diningV2Service.getDinings(
				new DiningV2GetDiningsQueryDto(1, 2, null, null, null, null), member.getId());
			DiningV2GetDiningsResponseDto secondPage = diningV2Service.getDinings(
				new DiningV2GetDiningsQueryDto(2, 2, null, null, null, null), member.getId());

			// then
			assertThat(meetingIds(firstPage)).containsExactly(latest, middle);
			assertThat(meetingIds(secondPage)).containsExactly(oldest);

			assertThat(firstPage.meta().getItemCount()).isEqualTo(3);
			assertThat(firstPage.meta().getPageCount()).isEqualTo(2);
			assertThat(firstPage.meta().isHasNextPage()).isTrue();
			assertThat(secondPage.meta().isHasPreviousPage()).isTrue();
			assertThat(secondPage.meta().isHasNextPage()).isFalse();
		}

		@Test
		@DisplayName("진행 중인 회식이 먼저, 그 안에서 회식 일자 최신순으로 정렬된다.")
		void getDinings_sortsInProgressFirstThenLatestDate() {
			// given
			Integer earlier = createDining(leader, "4월 30일 회식", "2024.04.30", MeetingJoinablePart.SERVER);
			Integer later = createDining(leader, "5월 10일 회식", "2024.05.10", MeetingJoinablePart.SERVER);
			Integer ended = createDining(leader, "5월 20일 회식", "2024.05.20", MeetingJoinablePart.SERVER);
			diningV2Service.endDining(ended, leader.getId());

			// when
			DiningV2GetDiningsResponseDto response = diningV2Service.getDinings(query(null, null, null, null),
				member.getId());

			// then
			assertThat(meetingIds(response)).containsExactly(later, earlier, ended);
			assertThat(response.meta().getItemCount()).isEqualTo(3);
			assertThat(response.dinings().get(0).diningDate()).isEqualTo(LocalDate.of(2024, 5, 10));
		}

		@Test
		@DisplayName("내 회식은 내가 개설했거나 현재 참석 중인 회식만 조회되고, 참석을 취소하면 제외된다.")
		void getDinings_myTab_containsOnlyLedOrAttendingDinings() {
			// given
			Integer ledByMember = createDining(member, "참석자가 연 회식", "2024.04.30", MeetingJoinablePart.SERVER);
			Integer attending = createDining(leader, "참석 중인 회식", "2024.04.30", MeetingJoinablePart.SERVER);
			Integer cancelled = createDining(leader, "취소한 회식", "2024.04.30", MeetingJoinablePart.SERVER);
			createDining(leader, "관계없는 회식", "2024.04.30", MeetingJoinablePart.SERVER);
			diningV2Service.applyAttendance(attending, firstRoundId(attending), member.getId());
			diningV2Service.applyAttendance(cancelled, firstRoundId(cancelled), member.getId());
			diningV2Service.cancelAttendance(cancelled, firstRoundId(cancelled), member.getId());

			// when
			DiningV2GetDiningsResponseDto response = diningV2Service.getDinings(
				query(DiningV2GetDiningsQueryDto.TAB_MY, null, null, null), member.getId());

			// then
			assertThat(meetingIds(response)).containsExactlyInAnyOrder(ledByMember, attending);
		}

		@Test
		@DisplayName("파트 필터는 해당 파트를 포함한 회식과 전체 파트 회식을 함께 조회한다.")
		void getDinings_partFilter_includesAllPartDinings() {
			// given
			Integer serverOnly = createDining(leader, "서버 회식", "2024.04.30", MeetingJoinablePart.SERVER);
			Integer webOnly = createDining(leader, "웹 회식", "2024.04.30", MeetingJoinablePart.WEB);
			Integer allParts = createDining(leader, "전체 회식", "2024.04.30", MeetingJoinablePart.values());

			// when
			DiningV2GetDiningsResponseDto response = diningV2Service.getDinings(
				query(null, null, MeetingJoinablePart.WEB, null), member.getId());

			// then
			assertThat(meetingIds(response)).containsExactlyInAnyOrder(webOnly, allParts).doesNotContain(serverOnly);
		}

		@Test
		@DisplayName("진행 상태와 회식명으로 필터링한다.")
		void getDinings_statusAndKeywordFilter() {
			// given
			Integer inProgress = createDining(leader, "서버 파트 회식", "2024.04.30", MeetingJoinablePart.SERVER);
			Integer ended = createDining(leader, "서버 파트 종강 회식", "2024.04.30", MeetingJoinablePart.SERVER);
			createDining(leader, "웹 파트 회식", "2024.04.30", MeetingJoinablePart.WEB);
			diningV2Service.endDining(ended, leader.getId());

			// when & then
			assertThat(meetingIds(diningV2Service.getDinings(
				query(null, DiningV2GetDiningsQueryDto.STATUS_ENDED, null, null), member.getId())))
				.containsExactly(ended);
			assertThat(meetingIds(diningV2Service.getDinings(
				query(null, DiningV2GetDiningsQueryDto.STATUS_IN_PROGRESS, null, "서버"), member.getId())))
				.containsExactly(inProgress);
		}

		@Test
		@DisplayName("카드의 인원은 전체 회차의 고유 참석자 수이고, 참석자 관리 버튼은 개설자에게만 노출된다.")
		void getDinings_cardShowsDistinctAttendeeCountAndManageFlag() {
			// given: 개설자는 1차·2차, 참석자는 1차·2차 모두 참석 → 고유 인원 2명
			Integer meetingId = createDining(leader, "서버 파트 회식", "2024.04.30", MeetingJoinablePart.SERVER);
			Integer secondRoundId = createRound(meetingId);
			diningV2Service.applyAttendance(meetingId, firstRoundId(meetingId), member.getId());
			diningV2Service.applyAttendance(meetingId, secondRoundId, member.getId());

			// when
			DiningCard leaderCard = diningV2Service.getDinings(query(null, null, null, null), leader.getId())
				.dinings().get(0);
			DiningCard memberCard = diningV2Service.getDinings(query(null, null, null, null), member.getId())
				.dinings().get(0);

			// then
			assertThat(leaderCard.attendeeCount()).isEqualTo(2);
			assertThat(leaderCard.leader().id()).isEqualTo(leader.getId());
			assertThat(leaderCard.canManageAttendees()).isTrue();
			assertThat(memberCard.canManageAttendees()).isFalse();
		}
	}

	@Nested
	class 상세 {

		@Test
		@DisplayName("회차 번호, 회차별 인원, 내 참석 상태를 반환하고 마감된 회차의 참석은 참석 완료로 표시한다.")
		void getDiningDetail_returnsRoundsWithMyStatus() {
			// given
			Integer meetingId = createDining(leader, "서버 파트 회식", "2024.04.30", MeetingJoinablePart.SERVER);
			Integer firstRoundId = firstRoundId(meetingId);
			Integer secondRoundId = createRound(meetingId);
			diningV2Service.applyAttendance(meetingId, firstRoundId, member.getId());
			diningV2Service.closeRound(meetingId, firstRoundId, leader.getId());

			// when
			DiningV2GetDiningDetailResponseDto detail = diningV2Service.getDiningDetail(meetingId, member.getId());

			// then
			assertThat(detail.isLeader()).isFalse();
			assertThat(detail.isApprovedPayer()).isFalse();
			assertThat(detail.leader().id()).isEqualTo(leader.getId());
			assertThat(detail.rounds()).extracting(DiningV2GetDiningDetailResponseDto.Round::roundNumber)
				.containsExactly(1, 2);

			DiningV2GetDiningDetailResponseDto.Round first = detail.rounds().get(0);
			assertThat(first.roundId()).isEqualTo(firstRoundId);
			assertThat(first.attendeeCount()).isEqualTo(2);
			assertThat(first.isClosed()).isTrue();
			assertThat(first.myAttendanceStatus()).isEqualTo(MyAttendanceStatus.ATTENDED);

			DiningV2GetDiningDetailResponseDto.Round second = detail.rounds().get(1);
			assertThat(second.roundId()).isEqualTo(secondRoundId);
			assertThat(second.attendeeCount()).isEqualTo(1);
			assertThat(second.myAttendanceStatus()).isEqualTo(MyAttendanceStatus.NOT_ATTENDING);
			assertThat(second.isNew()).isTrue();
			assertThat(first.isNew()).isFalse();
		}

		@Test
		@DisplayName("이전 회차에 참석했고 새 회차를 신청하지 않았을 때만 신규 회차 안내를 노출한다.")
		void getDiningDetail_showsNewRoundGuideOnlyForPreviousAttendee() {
			// given
			Integer meetingId = createDining(leader, "서버 파트 회식", "2024.04.30", MeetingJoinablePart.SERVER);
			diningV2Service.applyAttendance(meetingId, firstRoundId(meetingId), member.getId());
			Integer secondRoundId = createRound(meetingId);

			// when & then: 신청 전
			List<DiningV2GetDiningDetailResponseDto.Round> before = diningV2Service.getDiningDetail(meetingId,
				member.getId()).rounds();
			assertThat(before.get(0).showNewRoundGuide()).isFalse();
			assertThat(before.get(1).showNewRoundGuide()).isTrue();

			// when & then: 신청 후
			diningV2Service.applyAttendance(meetingId, secondRoundId, member.getId());
			assertThat(diningV2Service.getDiningDetail(meetingId, member.getId()).rounds().get(1).showNewRoundGuide())
				.isFalse();
		}

		@Test
		@DisplayName("개설자가 조회하면 개설자로 표시된다.")
		void getDiningDetail_byLeader() {
			// given
			Integer meetingId = createDining(leader, "서버 파트 회식", "2024.04.30", MeetingJoinablePart.SERVER);

			// when
			DiningV2GetDiningDetailResponseDto detail = diningV2Service.getDiningDetail(meetingId, leader.getId());

			// then
			assertThat(detail.isLeader()).isTrue();
			assertThat(detail.rounds().get(0).myAttendanceStatus()).isEqualTo(MyAttendanceStatus.ATTENDING);
		}
	}

	@Nested
	class 자동_확정 {

		@Test
		@DisplayName("회식 일자가 지난 회식의 미마감 회차만 마감한다.")
		void closeExpiredRounds_closesOnlyPastDiningRounds() {
			// given: 지난 날짜로 수정한 회식(회차 2개)과 아직 날짜가 남은 회식
			Integer pastDining = createDining(leader, "지난 회식", "2024.04.24", MeetingJoinablePart.SERVER);
			createRound(pastDining);
			diningV2Service.updateDining(pastDining, new DiningV2CreateAndUpdateDiningBodyDto(
				"지난 회식", null, null, null, "2024.04.20", null,
				new MeetingJoinablePart[] {MeetingJoinablePart.SERVER}, null), leader.getId());
			Integer upcomingDining = createDining(leader, "다가올 회식", "2024.04.30", MeetingJoinablePart.SERVER);

			// when
			int closedCount = diningV2Service.closeExpiredRounds();

			// then
			assertThat(closedCount).isEqualTo(2);
			assertThat(diningRoundRepository.findAllByMeetingIdOrderByIdAsc(pastDining))
				.allMatch(DiningRound::isClosed);
			assertThat(diningRoundRepository.findAllByMeetingIdOrderByIdAsc(upcomingDining))
				.noneMatch(DiningRound::isClosed);
		}

		@Test
		@DisplayName("오늘이 회식 일자인 회식은 아직 마감하지 않는다.")
		void closeExpiredRounds_keepsTodayDiningOpen() {
			// given
			Integer todayDining = createDining(leader, "오늘 회식", "2024.04.24", MeetingJoinablePart.SERVER);

			// when
			int closedCount = diningV2Service.closeExpiredRounds();

			// then
			assertThat(closedCount).isZero();
			assertThat(diningRoundRepository.findAllByMeetingIdOrderByIdAsc(todayDining))
				.noneMatch(DiningRound::isClosed);
		}
	}
}
