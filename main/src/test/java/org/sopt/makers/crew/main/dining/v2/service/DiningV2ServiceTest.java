package org.sopt.makers.crew.main.dining.v2.service;

import static org.assertj.core.api.Assertions.*;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateDiningBodyDto;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateRoundBodyDto;
import org.sopt.makers.crew.main.entity.dining.DiningAttendanceRepository;
import org.sopt.makers.crew.main.entity.dining.DiningDeletionHistory;
import org.sopt.makers.crew.main.entity.dining.DiningDeletionHistoryRepository;
import org.sopt.makers.crew.main.entity.dining.DiningRound;
import org.sopt.makers.crew.main.entity.dining.DiningRoundRepository;
import org.sopt.makers.crew.main.entity.dining.enums.DiningDeletionTargetType;
import org.sopt.makers.crew.main.entity.dining.enums.DiningRoundStatus;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.external.CaffeineTestConfig;
import org.sopt.makers.crew.main.global.annotation.IntegratedTest;
import org.sopt.makers.crew.main.global.config.ImageSettingProperties;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.ForbiddenException;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * StubTime 기준 현재 시각: 2024.04.24 00:00
 */
@IntegratedTest
@Import(CaffeineTestConfig.class)
class DiningV2ServiceTest {

	private static final String DINING_DATE = "2024.04.30";
	private static final String NAVER_LINK = "https://naver.me/abcdef";
	private static final String KAKAO_LINK = "https://kko.to/abcdef";
	private static final String IMAGE_URL = "https://image.test/dining.png";

	@Autowired
	private DiningV2Service diningV2Service;
	@Autowired
	private MeetingRepository meetingRepository;
	@Autowired
	private DiningRoundRepository diningRoundRepository;
	@Autowired
	private DiningAttendanceRepository diningAttendanceRepository;
	@Autowired
	private DiningDeletionHistoryRepository diningDeletionHistoryRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ImageSettingProperties imageSettingProperties;

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

	private DiningV2CreateAndUpdateDiningBodyDto diningBody(String diningDate, String place, String startTime,
		String naverMapLink, List<String> files) {
		return new DiningV2CreateAndUpdateDiningBodyDto(
			"서버 파트 회식",
			place,
			naverMapLink,
			KAKAO_LINK,
			diningDate,
			startTime,
			new MeetingJoinablePart[] {MeetingJoinablePart.SERVER},
			files
		);
	}

	private DiningV2CreateAndUpdateRoundBodyDto roundBody(String place) {
		return new DiningV2CreateAndUpdateRoundBodyDto(place, NAVER_LINK, null, "21:00");
	}

	private Integer createDining() {
		return diningV2Service.createDining(
			diningBody(DINING_DATE, "1차 장소", "18:00", NAVER_LINK, List.of(IMAGE_URL)), leader.getId()
		).meetingId();
	}

	private List<DiningRound> getRounds(Integer meetingId) {
		return diningRoundRepository.findAllByMeetingIdOrderByIdAsc(meetingId);
	}

	@Nested
	class 회식_등록 {

		@Test
		@DisplayName("회식을 등록하면 회식과 1차 회차가 생성되고 개설자가 1차에 참석한다.")
		void createDining_createsDiningFirstRoundAndLeaderAttendance() {
			// when
			Integer meetingId = createDining();

			// then
			Meeting dining = meetingRepository.findDiningByIdOrThrow(meetingId);
			assertThat(dining.getCategory()).isEqualTo(MeetingCategory.DINING);
			assertThat(dining.getUserId()).isEqualTo(leader.getId());
			assertThat(dining.getmStartDate()).isEqualTo(LocalDateTime.of(2024, 4, 30, 0, 0));
			assertThat(dining.getImageURL().get(0).getUrl()).isEqualTo(IMAGE_URL);
			assertThat(dining.isDiningEnded()).isFalse();

			List<DiningRound> rounds = getRounds(meetingId);
			assertThat(rounds).hasSize(1);
			assertThat(rounds.get(0).getPlace()).isEqualTo("1차 장소");
			assertThat(rounds.get(0).getStartTime()).isEqualTo(LocalTime.of(18, 0));
			assertThat(rounds.get(0).getStatus()).isEqualTo(DiningRoundStatus.OPEN);
			assertThat(diningAttendanceRepository.existsByRoundIdAndUserId(rounds.get(0).getId(), leader.getId()))
				.isTrue();
		}

		@Test
		@DisplayName("대표 이미지가 없으면 기본 이미지로 등록된다.")
		void createDining_withoutImage_usesDefaultImage() {
			// when
			Integer meetingId = diningV2Service.createDining(
				diningBody(DINING_DATE, null, null, null, null), leader.getId()
			).meetingId();

			// then
			Meeting dining = meetingRepository.findDiningByIdOrThrow(meetingId);
			assertThat(dining.getImageURL()).hasSize(1);
			assertThat(dining.getImageURL().get(0).getUrl()).isEqualTo(imageSettingProperties.getDefaultDiningImage());
			assertThat(getRounds(meetingId).get(0).getPlace()).isNull();
			assertThat(getRounds(meetingId).get(0).getStartTime()).isNull();
		}

		@Test
		@DisplayName("지난 날짜로는 회식을 등록할 수 없다.")
		void createDining_withPastDate_throwsException() {
			assertThatThrownBy(() -> diningV2Service.createDining(
				diningBody("2024.04.23", null, null, null, null), leader.getId()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(PAST_DINING_DATE.getErrorCode());
		}

		@Test
		@DisplayName("네이버지도, 카카오맵이 아닌 링크는 등록할 수 없다.")
		void createDining_withInvalidMapLink_throwsException() {
			assertThatThrownBy(() -> diningV2Service.createDining(
				diningBody(DINING_DATE, null, null, "https://maps.google.com/abc", null), leader.getId()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(INVALID_DINING_MAP_LINK.getErrorCode());
		}

		@Test
		@DisplayName("회식은 기존 모임 조회로 찾을 수 없다.")
		void createdDining_isNotFoundByMeetingApi() {
			// given
			Integer meetingId = createDining();

			// when & then
			assertThatThrownBy(() -> meetingRepository.findByIdOrThrow(meetingId))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining(NOT_FOUND_MEETING.getErrorCode());
		}
	}

	@Nested
	class 회식_수정 {

		@Test
		@DisplayName("실제로 바뀐 항목만 수정됨으로 표시된다.")
		void updateDining_marksOnlyChangedFields() {
			// given
			Integer meetingId = createDining();

			// when: 일자, 시작 시간만 변경
			diningV2Service.updateDining(meetingId,
				diningBody("2024.05.01", "1차 장소", "19:00", NAVER_LINK, List.of(IMAGE_URL)), leader.getId());

			// then
			Meeting dining = meetingRepository.findDiningByIdOrThrow(meetingId);
			DiningRound firstRound = getRounds(meetingId).get(0);
			assertThat(dining.isDateModified()).isTrue();
			assertThat(dining.getmStartDate()).isEqualTo(LocalDateTime.of(2024, 5, 1, 0, 0));
			assertThat(dining.getEndDate()).isEqualTo(LocalDateTime.of(2024, 5, 1, 23, 59, 59));
			assertThat(firstRound.isStartTimeModified()).isTrue();
			assertThat(firstRound.isPlaceModified()).isFalse();
			assertThat(firstRound.isLinkModified()).isFalse();
		}

		@Test
		@DisplayName("수정할 때는 지난 날짜도 허용한다.")
		void updateDining_withPastDate_succeeds() {
			// given
			Integer meetingId = createDining();

			// when
			diningV2Service.updateDining(meetingId,
				diningBody("2024.04.01", "1차 장소", "18:00", NAVER_LINK, List.of(IMAGE_URL)), leader.getId());

			// then
			assertThat(meetingRepository.findDiningByIdOrThrow(meetingId).getmStartDate())
				.isEqualTo(LocalDateTime.of(2024, 4, 1, 0, 0));
		}

		@Test
		@DisplayName("개설자가 아니면 수정할 수 없다.")
		void updateDining_byNonLeader_throwsException() {
			// given
			Integer meetingId = createDining();

			// when & then
			assertThatThrownBy(() -> diningV2Service.updateDining(meetingId,
				diningBody(DINING_DATE, null, null, null, null), member.getId()))
				.isInstanceOf(ForbiddenException.class);
		}

		@Test
		@DisplayName("종료된 회식은 수정할 수 없다.")
		void updateDining_whenEnded_throwsException() {
			// given
			Integer meetingId = createDining();
			diningV2Service.endDining(meetingId, leader.getId());

			// when & then
			assertThatThrownBy(() -> diningV2Service.updateDining(meetingId,
				diningBody(DINING_DATE, null, null, null, null), leader.getId()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(ENDED_DINING.getErrorCode());
		}
	}

	@Nested
	class 회식_삭제 {

		@Test
		@DisplayName("회식을 삭제하면 삭제 이력을 남기고 회식, 회차, 참석 기록을 모두 삭제한다.")
		void deleteDining_recordsHistoryAndDeletesAll() {
			// given
			Integer meetingId = createDining();
			diningV2Service.createRound(meetingId, roundBody("2차 장소"), leader.getId());
			List<Integer> roundIds = getRounds(meetingId).stream().map(DiningRound::getId).toList();

			// when
			diningV2Service.deleteDining(meetingId, leader.getId());

			// then
			assertThat(meetingRepository.findById(meetingId)).isEmpty();
			assertThat(getRounds(meetingId)).isEmpty();
			assertThat(diningAttendanceRepository.findAllByRoundIdIn(roundIds)).isEmpty();

			List<DiningDeletionHistory> histories = diningDeletionHistoryRepository.findAll();
			assertThat(histories).hasSize(1);
			DiningDeletionHistory history = histories.get(0);
			assertThat(history.getTargetType()).isEqualTo(DiningDeletionTargetType.DINING);
			assertThat(history.getTargetId()).isEqualTo(meetingId);
			assertThat(history.getDeletedByUserId()).isEqualTo(leader.getId());
			assertThat(history.getSnapshot().get("schemaVersion"))
				.isEqualTo(DiningDeletionHistory.SNAPSHOT_SCHEMA_VERSION);
			Map<?, ?> data = (Map<?, ?>)history.getSnapshot().get("data");
			assertThat((List<?>)data.get("rounds")).hasSize(2);
		}

		@Test
		@DisplayName("마감된 회차가 있으면 회식을 삭제할 수 없다.")
		void deleteDining_withClosedRound_throwsException() {
			// given
			Integer meetingId = createDining();
			diningV2Service.closeRound(meetingId, getRounds(meetingId).get(0).getId(), leader.getId());

			// when & then
			assertThatThrownBy(() -> diningV2Service.deleteDining(meetingId, leader.getId()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(CANNOT_DELETE_DINING_WITH_CLOSED_ROUND.getErrorCode());
		}
	}

	@Nested
	class 회차 {

		@Test
		@DisplayName("회차를 추가하면 개설자가 새 회차에 자동 참석한다.")
		void createRound_leaderAttendsNewRound() {
			// given
			Integer meetingId = createDining();

			// when
			Integer roundId = diningV2Service.createRound(meetingId, roundBody("2차 장소"), leader.getId()).roundId();

			// then
			List<DiningRound> rounds = getRounds(meetingId);
			assertThat(rounds).hasSize(2);
			assertThat(rounds.get(1).getId()).isEqualTo(roundId);
			assertThat(diningAttendanceRepository.existsByRoundIdAndUserId(roundId, leader.getId())).isTrue();
		}

		@Test
		@DisplayName("마감된 회차도 수정할 수 있다.")
		void updateRound_whenClosed_succeeds() {
			// given
			Integer meetingId = createDining();
			Integer roundId = diningV2Service.createRound(meetingId, roundBody("2차 장소"), leader.getId()).roundId();
			diningV2Service.closeRound(meetingId, roundId, leader.getId());

			// when
			diningV2Service.updateRound(meetingId, roundId, roundBody("바뀐 2차 장소"), leader.getId());

			// then
			DiningRound round = diningRoundRepository.findByIdOrThrow(roundId);
			assertThat(round.getPlace()).isEqualTo("바뀐 2차 장소");
			assertThat(round.isPlaceModified()).isTrue();
		}

		@Test
		@DisplayName("다른 회식의 회차는 수정할 수 없다.")
		void updateRound_ofOtherDining_throwsException() {
			// given
			Integer meetingId = createDining();
			Integer otherMeetingId = createDining();
			Integer otherRoundId = getRounds(otherMeetingId).get(0).getId();

			// when & then
			assertThatThrownBy(() -> diningV2Service.updateRound(meetingId, otherRoundId, roundBody("장소"),
				leader.getId()))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining(NOT_FOUND_DINING_ROUND.getErrorCode());
		}

		@Test
		@DisplayName("회차를 삭제하면 삭제 이력을 남기고 회차와 참석 기록을 삭제한다.")
		void deleteRound_recordsHistoryAndDeletes() {
			// given
			Integer meetingId = createDining();
			Integer roundId = diningV2Service.createRound(meetingId, roundBody("2차 장소"), leader.getId()).roundId();

			// when
			diningV2Service.deleteRound(meetingId, roundId, leader.getId());

			// then
			assertThat(getRounds(meetingId)).hasSize(1);
			assertThat(diningAttendanceRepository.findAllByRoundId(roundId)).isEmpty();
			List<DiningDeletionHistory> histories = diningDeletionHistoryRepository.findAll();
			assertThat(histories).hasSize(1);
			assertThat(histories.get(0).getTargetType()).isEqualTo(DiningDeletionTargetType.ROUND);
			assertThat(histories.get(0).getTargetId()).isEqualTo(roundId);
		}

		@Test
		@DisplayName("1차는 회차 삭제로 삭제할 수 없다.")
		void deleteRound_firstRound_throwsException() {
			// given
			Integer meetingId = createDining();
			Integer firstRoundId = getRounds(meetingId).get(0).getId();

			// when & then
			assertThatThrownBy(() -> diningV2Service.deleteRound(meetingId, firstRoundId, leader.getId()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(CANNOT_DELETE_FIRST_DINING_ROUND.getErrorCode());
		}

		@Test
		@DisplayName("마감된 회차는 삭제할 수 없다.")
		void deleteRound_closedRound_throwsException() {
			// given
			Integer meetingId = createDining();
			Integer roundId = diningV2Service.createRound(meetingId, roundBody("2차 장소"), leader.getId()).roundId();
			diningV2Service.closeRound(meetingId, roundId, leader.getId());

			// when & then
			assertThatThrownBy(() -> diningV2Service.deleteRound(meetingId, roundId, leader.getId()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(CANNOT_DELETE_CLOSED_DINING_ROUND.getErrorCode());
		}
	}

	@Nested
	class 마감과_종료 {

		@Test
		@DisplayName("이미 마감된 회차는 다시 마감할 수 없다.")
		void closeRound_twice_throwsException() {
			// given
			Integer meetingId = createDining();
			Integer roundId = getRounds(meetingId).get(0).getId();
			diningV2Service.closeRound(meetingId, roundId, leader.getId());

			// when & then
			assertThatThrownBy(() -> diningV2Service.closeRound(meetingId, roundId, leader.getId()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(ALREADY_CLOSED_DINING_ROUND.getErrorCode());
		}

		@Test
		@DisplayName("회식을 종료하면 마감하지 않은 회차가 모두 마감되고 회차를 추가할 수 없다.")
		void endDining_closesOpenRoundsAndBlocksNewRound() {
			// given
			Integer meetingId = createDining();
			Integer secondRoundId = diningV2Service.createRound(meetingId, roundBody("2차 장소"), leader.getId())
				.roundId();
			diningV2Service.closeRound(meetingId, secondRoundId, leader.getId());

			// when
			diningV2Service.endDining(meetingId, leader.getId());

			// then
			assertThat(meetingRepository.findDiningByIdOrThrow(meetingId).isDiningEnded()).isTrue();
			assertThat(getRounds(meetingId)).allMatch(DiningRound::isClosed);
			assertThatThrownBy(() -> diningV2Service.createRound(meetingId, roundBody("3차 장소"), leader.getId()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(ENDED_DINING.getErrorCode());
		}
	}
}
