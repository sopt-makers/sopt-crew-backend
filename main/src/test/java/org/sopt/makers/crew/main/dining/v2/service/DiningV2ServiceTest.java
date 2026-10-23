package org.sopt.makers.crew.main.dining.v2.service;

import static org.assertj.core.api.Assertions.*;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.sopt.makers.crew.main.dining.v2.dto.query.DiningV2GetAttendeesQueryDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.sopt.makers.crew.main.external.notification.PushNotificationService;
import org.sopt.makers.crew.main.external.notification.dto.request.PushNotificationRequestDto;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
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
import org.sopt.makers.crew.main.global.exception.UnAuthorizedException;
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

	@Autowired
	private DiningNotificationSender diningNotificationSender;
	@MockBean
	private PushNotificationService pushNotificationService;

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
		@DisplayName("참여 파트의 중복은 제거되고 파트 순서대로 저장된다.")
		void createDining_withDuplicateParts_savesDistinctParts() {
			// given
			DiningV2CreateAndUpdateDiningBodyDto body = new DiningV2CreateAndUpdateDiningBodyDto(
				"서버 파트 회식", null, null, null, DINING_DATE, null,
				new MeetingJoinablePart[] {MeetingJoinablePart.WEB, MeetingJoinablePart.SERVER,
					MeetingJoinablePart.WEB},
				null
			);

			// when
			Integer meetingId = diningV2Service.createDining(body, leader.getId()).meetingId();

			// then
			assertThat(meetingRepository.findDiningByIdOrThrow(meetingId).getJoinableParts())
				.containsExactly(MeetingJoinablePart.SERVER, MeetingJoinablePart.WEB);
		}

		@Test
		@DisplayName("참여 파트에 null 이 있으면 등록할 수 없다.")
		void createDining_withNullPart_throwsException() {
			// given
			DiningV2CreateAndUpdateDiningBodyDto body = new DiningV2CreateAndUpdateDiningBodyDto(
				"서버 파트 회식", null, null, null, DINING_DATE, null,
				new MeetingJoinablePart[] {MeetingJoinablePart.SERVER, null},
				null
			);

			// when & then
			assertThatThrownBy(() -> diningV2Service.createDining(body, leader.getId()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(VALIDATION_EXCEPTION.getErrorCode());
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

	@Nested
	class 참석 {
		private Integer meetingId;
		private Integer roundId;

		@BeforeEach
		void createRound() {
			meetingId = createDining();
			roundId = getRounds(meetingId).get(0).getId();
		}

		@Test
		@DisplayName("참석 신청은 회차별로 독립적이고 현황은 현재 참석만 반영한다.")
		void attendance_isIndependentPerRound() {
			Integer secondRoundId = diningV2Service.createRound(meetingId, roundBody("2차"), leader.getId()).roundId();
			diningV2Service.applyAttendance(meetingId, secondRoundId, member.getId());
			assertThat(diningV2Service.getAttendance(meetingId, roundId, member.getId()).isAttending()).isFalse();
			assertThat(diningV2Service.getAttendance(meetingId, secondRoundId, member.getId()).isAttending()).isTrue();
			assertThat(diningV2Service.getAttendance(meetingId, secondRoundId, member.getId()).attendeeCount())
				.isEqualTo(2);
			diningV2Service.applyAttendance(meetingId, roundId, member.getId());
			diningV2Service.cancelAttendance(meetingId, secondRoundId, member.getId());
			assertThat(diningV2Service.getAttendance(meetingId, roundId, member.getId()).isAttending()).isTrue();
			assertThat(diningV2Service.getAttendance(meetingId, secondRoundId, member.getId()).attendeeCount())
				.isEqualTo(1);
		}

		@ParameterizedTest
		@ValueSource(strings = {"closed", "ended"})
		@DisplayName("회차 마감 또는 회식 종료 후 본인 신청과 취소는 차단되며 참석과 이력은 변경되지 않는다.")
		void selfAttendance_rejectsClosedRound(String state) {
			if (state.equals("ended")) {
				diningV2Service.endDining(meetingId, leader.getId());
			} else {
				diningV2Service.closeRound(meetingId, roundId, leader.getId());
			}

			assertThatThrownBy(() -> diningV2Service.applyAttendance(meetingId, roundId, member.getId()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(ALREADY_CLOSED_DINING_ROUND.getErrorCode());
			assertThatThrownBy(() -> diningV2Service.cancelAttendance(meetingId, roundId, leader.getId()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(ALREADY_CLOSED_DINING_ROUND.getErrorCode());
			assertThat(diningAttendanceRepository.existsByRoundIdAndUserId(roundId, member.getId())).isFalse();
			assertThat(diningAttendanceRepository.existsByRoundIdAndUserId(roundId, leader.getId())).isTrue();
			assertThat(diningDeletionHistoryRepository.findAll()).isEmpty();
		}

		@ParameterizedTest
		@ValueSource(strings = {"closed", "ended"})
		@DisplayName("마감과 종료 후에도 개설자와 승인된 공동 결제자는 참석자를 추가하고 삭제할 수 있다.")
		void management_allowsClosedRound(String state) {
			diningV2Service.applyAttendance(meetingId, roundId, member.getId());
			var payer = diningAttendanceRepository.findByRoundIdAndUserId(roundId, member.getId()).orElseThrow();
			payer.requestPayer(LocalDateTime.of(2024, 4, 24, 0, 0));
			payer.approvePayer();
			if (state.equals("ended")) {
				diningV2Service.endDining(meetingId, leader.getId());
			} else {
				diningV2Service.closeRound(meetingId, roundId, leader.getId());
			}
			User third = userRepository.save(createUser("추가 참석자", 3));
			for (Integer managerId : List.of(leader.getId(), member.getId())) {
				diningV2Service.addAttendee(meetingId, roundId, third.getId(), managerId);
				assertThat(diningAttendanceRepository.existsByRoundIdAndUserId(roundId, third.getId())).isTrue();
				diningV2Service.deleteAttendee(meetingId, roundId, third.getId(), managerId);
				assertThat(diningAttendanceRepository.existsByRoundIdAndUserId(roundId, third.getId())).isFalse();
			}
		}

		@Test
		@DisplayName("비참석 사용자도 모달 목록을 조회할 수 있으며 연락처와 기수는 노출하지 않는다.")
		void modal_isAvailableToNonAttendeeWithoutPrivateFields() {
			var response = diningV2Service.getAttendanceAttendees(meetingId, roundId, member.getId());
			assertThat(response.attendees()).extracting(attendee -> attendee.id()).containsExactly(leader.getId());
			var json = new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(response);
			assertThat(json.get("attendees").get(0).has("phone")).isFalse();
			assertThat(json.get("attendees").get(0).has("recentGeneration")).isFalse();
			assertThatThrownBy(() -> diningV2Service.getAttendees(meetingId, roundId,
				new DiningV2GetAttendeesQueryDto(null, null, null), member.getId()))
				.isInstanceOf(ForbiddenException.class);
		}

		@Test
		@DisplayName("관리자 목록은 등록 최신순과 등록 순서 번호를 사용하고 최근 기수 및 연락처를 제공한다.")
		void management_ordersByRegistrationAndIncludesDetails() {
			User third = userRepository.save(User.builder().name("세 번째").orgId(3).phone("010-3333-3333")
				.activities(List.of(new UserActivityVO("서버", 36), new UserActivityVO("웹", 30))).build());
			diningV2Service.applyAttendance(meetingId, roundId, third.getId());
			diningV2Service.applyAttendance(meetingId, roundId, member.getId());
			// 사용자 ID와 등록 순서가 다르고 시각이 같아도 참석 ID로 순서가 고정된다.
			for (var attendance : diningAttendanceRepository.findAllByRoundId(roundId)) {
				attendance.createdTimestamp = LocalDateTime.of(2024, 4, 24, 0, 0);
			}
			var response = diningV2Service.getAttendees(meetingId, roundId,
				new DiningV2GetAttendeesQueryDto(null, null, null), leader.getId());
			assertThat(response.attendees()).extracting(attendee -> attendee.id())
				.containsExactly(member.getId(), third.getId(), leader.getId());
			assertThat(response.attendees()).extracting(attendee -> attendee.attendanceNumber())
				.containsExactly(3L, 2L, 1L);
			assertThat(response.attendees().get(1).recentGeneration()).isEqualTo(36);
			assertThat(response.attendees().get(1).phone()).isEqualTo("010-3333-3333");
			assertThat(response.meta().getItemCount()).isEqualTo(3);
		}

		@Test
		@DisplayName("관리자 목록은 페이지 경계에서도 등록 번호를 유지하고 빈 페이지의 총 개수를 보존한다.")
		void management_paginatesWithStableNumbers() {
			for (int i = 3; i < 14; i++) {
				User attendee = userRepository.save(createUser("참석자" + i, i));
				diningV2Service.addAttendee(meetingId, roundId, attendee.getId(), leader.getId());
			}
			var first = diningV2Service.getAttendees(meetingId, roundId,
				new DiningV2GetAttendeesQueryDto(1, 10, "desc"), leader.getId());
			var second = diningV2Service.getAttendees(meetingId, roundId,
				new DiningV2GetAttendeesQueryDto(2, 10, "desc"), leader.getId());
			assertThat(first.attendees()).hasSize(10);
			assertThat(first.attendees().get(0).attendanceNumber()).isEqualTo(12);
			assertThat(first.meta().isHasNextPage()).isTrue();
			assertThat(second.attendees()).extracting(attendee -> attendee.attendanceNumber()).containsExactly(2L, 1L);
			assertThat(second.meta().getPageCount()).isEqualTo(2);
			assertThat(second.meta().isHasPreviousPage()).isTrue();
			var empty = diningV2Service.getAttendees(meetingId, roundId,
				new DiningV2GetAttendeesQueryDto(3, 10, "desc"), leader.getId());
			assertThat(empty.attendees()).isEmpty();
			assertThat(empty.meta().getItemCount()).isEqualTo(12);
			var ascending = diningV2Service.getAttendees(meetingId, roundId,
				new DiningV2GetAttendeesQueryDto(1, 30, "asc"), leader.getId());
			assertThat(ascending.attendees()).hasSize(12);
			assertThat(ascending.attendees().get(0).id()).isEqualTo(leader.getId());
			assertThat(ascending.attendees().get(0).attendanceNumber()).isEqualTo(1);
		}

		@Test
		@DisplayName("다른 파트의 OB 사용자도 신청 및 직접 추가가 가능하다.")
		void attendance_acceptsOtherPartAndOldGeneration() {
			User ob = userRepository.save(User.builder().name("OB 웹").orgId(3)
				.activities(List.of(new UserActivityVO("웹", 1))).build());
			diningV2Service.applyAttendance(meetingId, roundId, ob.getId());
			assertThat(diningAttendanceRepository.existsByRoundIdAndUserId(roundId, ob.getId())).isTrue();
			diningV2Service.cancelAttendance(meetingId, roundId, ob.getId());
			diningV2Service.addAttendee(meetingId, roundId, ob.getId(), leader.getId());
			assertThat(diningAttendanceRepository.existsByRoundIdAndUserId(roundId, ob.getId())).isTrue();
		}

		@Test
		@DisplayName("신청과 직접 추가 모두 같은 회차에 중복 참석할 수 없다.")
		void attendance_rejectsDuplicate() {
			diningV2Service.applyAttendance(meetingId, roundId, member.getId());
			assertThatThrownBy(() -> diningV2Service.applyAttendance(meetingId, roundId, member.getId()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(ALREADY_ATTENDED_DINING_ROUND.getErrorCode());
			assertThatThrownBy(() -> diningV2Service.addAttendee(meetingId, roundId, member.getId(), leader.getId()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(ALREADY_ATTENDED_DINING_ROUND.getErrorCode());
			assertThat(diningAttendanceRepository.countByRoundId(roundId)).isEqualTo(2);
		}

		@Test
		@DisplayName("존재하지 않는 사용자는 신청하거나 직접 추가할 수 없다.")
		void attendance_requiresExistingUser() {
			assertThatThrownBy(() -> diningV2Service.applyAttendance(meetingId, roundId, -1))
				.isInstanceOf(UnAuthorizedException.class);
			assertThatThrownBy(() -> diningV2Service.addAttendee(meetingId, roundId, -1, leader.getId()))
				.isInstanceOf(UnAuthorizedException.class);
		}

		@Test
		@DisplayName("신청 취소는 본인을 삭제 주체로 기록하고 현재 참석자를 삭제한다.")
		void cancelAttendance_recordsSnapshot() {
			diningV2Service.applyAttendance(meetingId, roundId, member.getId());
			Integer attendanceId = diningAttendanceRepository.findByRoundIdAndUserId(roundId, member.getId())
				.orElseThrow().getId();
			diningV2Service.cancelAttendance(meetingId, roundId, member.getId());
			diningAttendanceRepository.flush();
			assertThat(diningAttendanceRepository.findById(attendanceId)).isEmpty();
			DiningDeletionHistory history = diningDeletionHistoryRepository.findAll().get(0);
			assertThat(history.getTargetType()).isEqualTo(DiningDeletionTargetType.ATTENDANCE);
			assertThat(history.getTargetId()).isEqualTo(attendanceId);
			assertThat(history.getMeetingId()).isEqualTo(meetingId);
			assertThat(history.getDeletedByUserId()).isEqualTo(member.getId());
			assertThat(((Map<?, ?>)history.getSnapshot().get("data")).get("userId")).isEqualTo(member.getId());
			assertThat(diningV2Service.getAttendees(meetingId, roundId, new DiningV2GetAttendeesQueryDto(null, null, null), leader.getId()).attendees())
				.extracting(attendee -> attendee.id()).containsExactly(leader.getId());
		}

		@Test
		@DisplayName("참석하지 않은 회차의 취소와 삭제는 참석 기록 없음 예외를 반환한다.")
		void deletion_requiresAttendance() {
			assertThatThrownBy(() -> diningV2Service.cancelAttendance(meetingId, roundId, member.getId()))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining(NOT_FOUND_DINING_ATTENDANCE.getErrorCode());
			assertThatThrownBy(() -> diningV2Service.deleteAttendee(meetingId, roundId, member.getId(), leader.getId()))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining(NOT_FOUND_DINING_ATTENDANCE.getErrorCode());
			assertThat(diningDeletionHistoryRepository.findAll()).isEmpty();
		}

		@Test
		@DisplayName("종료 후에도 개설자는 자신을 삭제하고 참석자를 추가·삭제할 수 있다.")
		void management_afterEndAndLeaderRemoval() {
			diningV2Service.endDining(meetingId, leader.getId());
			diningV2Service.deleteAttendee(meetingId, roundId, leader.getId(), leader.getId());
			assertThat(diningV2Service.getAttendees(meetingId, roundId, new DiningV2GetAttendeesQueryDto(null, null, null), leader.getId()).attendees()).isEmpty();
			diningV2Service.addAttendee(meetingId, roundId, member.getId(), leader.getId());
			assertThat(diningV2Service.getAttendees(meetingId, roundId, new DiningV2GetAttendeesQueryDto(null, null, null), leader.getId()).attendees())
				.extracting(attendee -> attendee.id()).containsExactly(member.getId());
			diningV2Service.deleteAttendee(meetingId, roundId, member.getId(), leader.getId());
			assertThat(diningAttendanceRepository.countByRoundId(roundId)).isZero();
			assertThat(diningAttendanceRepository.countDistinctUserIdsByMeetingId(meetingId)).isZero();
			assertThat(diningDeletionHistoryRepository.findAll()).anySatisfy(history -> {
				assertThat(history.getDeletedByUserId()).isEqualTo(leader.getId());
				assertThat(((Map<?, ?>)history.getSnapshot().get("data")).get("userId")).isEqualTo(member.getId());
			});
		}

		@Test
		@DisplayName("일반 참석자는 참석자 조회·추가·삭제 권한이 없다.")
		void management_requiresPermission() {
			diningV2Service.applyAttendance(meetingId, roundId, member.getId());
			assertThatThrownBy(() -> diningV2Service.getAttendees(meetingId, roundId, new DiningV2GetAttendeesQueryDto(null, null, null), member.getId()))
				.isInstanceOf(ForbiddenException.class);
			assertThatThrownBy(() -> diningV2Service.addAttendee(meetingId, roundId, member.getId(), member.getId()))
				.isInstanceOf(ForbiddenException.class);
			assertThatThrownBy(() -> diningV2Service.deleteAttendee(meetingId, roundId, leader.getId(), member.getId()))
				.isInstanceOf(ForbiddenException.class);
			assertThat(diningAttendanceRepository.countByRoundId(roundId)).isEqualTo(2);
			assertThat(diningDeletionHistoryRepository.findAll()).isEmpty();
		}

		@Test
		@DisplayName("승인된 공동 결제자는 참석자 관리 권한을 사용하고 개설자도 삭제할 수 있다.")
		void management_reusesApprovedPayerPermission() {
			diningV2Service.applyAttendance(meetingId, roundId, member.getId());
			var attendance = diningAttendanceRepository.findByRoundIdAndUserId(roundId, member.getId()).orElseThrow();
			attendance.requestPayer(LocalDateTime.of(2024, 4, 24, 0, 0));
			assertThatThrownBy(() -> diningV2Service.getAttendees(meetingId, roundId, new DiningV2GetAttendeesQueryDto(null, null, null), member.getId()))
				.isInstanceOf(ForbiddenException.class);
			attendance.approvePayer();
			diningV2Service.deleteAttendee(meetingId, roundId, leader.getId(), member.getId());
			diningV2Service.addAttendee(meetingId, roundId, leader.getId(), member.getId());
			assertThat(diningV2Service.getAttendees(meetingId, roundId, new DiningV2GetAttendeesQueryDto(null, null, null), member.getId()).attendees()).hasSize(2);
		}

		@Test
		@DisplayName("회식 전체 인원은 현재 참석자의 고유 수이며 삭제 이력은 포함하지 않는다.")
		void countDistinctAttendees() {
			User third = userRepository.save(createUser("세 번째", 3));
			Integer second = diningV2Service.createRound(meetingId, roundBody("2차"), leader.getId()).roundId();
			diningV2Service.applyAttendance(meetingId, roundId, member.getId());
			diningV2Service.applyAttendance(meetingId, second, third.getId());
			assertThat(diningAttendanceRepository.countDistinctUserIdsByMeetingId(meetingId)).isEqualTo(3);
			diningV2Service.cancelAttendance(meetingId, roundId, member.getId());
			assertThat(diningAttendanceRepository.countDistinctUserIdsByMeetingId(meetingId)).isEqualTo(2);
			assertThat(diningAttendanceRepository.countDistinctUserIdsByMeetingId(createDining())).isEqualTo(1);
		}

		@Test
		@DisplayName("목록용 고유 참석자 수는 회식별로 집계하며 취소 이력과 요청하지 않은 회식은 제외한다.")
		void countDistinctAttendees_inBatch() {
			User third = userRepository.save(createUser("세 번째", 3));
			Integer secondRound = diningV2Service.createRound(meetingId, roundBody("2차"), leader.getId()).roundId();
			diningV2Service.applyAttendance(meetingId, roundId, member.getId());
			diningV2Service.applyAttendance(meetingId, secondRound, third.getId());
			Integer otherDining = createDining();
			Integer otherRound = getRounds(otherDining).get(0).getId();
			diningV2Service.applyAttendance(otherDining, otherRound, member.getId());
			diningV2Service.cancelAttendance(otherDining, otherRound, member.getId());
			Integer emptyDining = createDining();
			diningV2Service.cancelAttendance(emptyDining, getRounds(emptyDining).get(0).getId(), leader.getId());
			createDining();

			var counts = diningAttendanceRepository.countDistinctUserIdsByMeetingIdIn(
				List.of(meetingId, otherDining, emptyDining));

			assertThat(counts).extracting(count -> tuple(count.getMeetingId(), count.getAttendeeCount()))
				.containsExactlyInAnyOrder(tuple(meetingId, 3L), tuple(otherDining, 1L));
			assertThat(diningAttendanceRepository.countDistinctUserIdsByMeetingIdIn(List.of())).isEmpty();
		}

		@ParameterizedTest
		@ValueSource(strings = {"apply", "cancel", "status", "list", "modal", "add", "delete"})
		@DisplayName("모든 참석 API는 다른 회식에 속한 회차를 거절한다.")
		void attendance_rejectsForeignRound(String operation) {
			Integer otherRoundId = getRounds(createDining()).get(0).getId();
			assertThatThrownBy(() -> callAttendanceApi(operation, meetingId, otherRoundId))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining(NOT_FOUND_DINING_ROUND.getErrorCode());
		}

		@ParameterizedTest
		@ValueSource(strings = {"apply", "cancel", "status", "list", "modal", "add", "delete"})
		@DisplayName("모든 참석 API는 존재하지 않는 회차를 거절한다.")
		void attendance_rejectsMissingRound(String operation) {
			assertThatThrownBy(() -> callAttendanceApi(operation, meetingId, -1))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining(NOT_FOUND_DINING_ROUND.getErrorCode());
		}

		@ParameterizedTest
		@ValueSource(strings = {"apply", "cancel", "status", "list", "modal", "add", "delete"})
		@DisplayName("모든 참석 API는 일반 모임을 회식으로 처리하지 않는다.")
		void attendance_rejectsNonDiningMeeting(String operation) {
			ReflectionTestUtils.setField(meetingRepository.findDiningByIdOrThrow(meetingId), "category",
				MeetingCategory.STUDY);
			assertThatThrownBy(() -> callAttendanceApi(operation, meetingId, roundId))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining(NOT_FOUND_DINING.getErrorCode());
		}

		@Test
		@DisplayName("취소와 회차 삭제 이력의 참석자는 새 회차 알림에 포함하고 개설자와 중복은 제외한다.")
		void notification_includesHistoricalAttendees() {
			diningV2Service.applyAttendance(meetingId, roundId, member.getId());
			diningV2Service.cancelAttendance(meetingId, roundId, member.getId());
			diningV2Service.cancelAttendance(meetingId, roundId, leader.getId());
			Integer deletedRound = diningV2Service.createRound(meetingId, roundBody("삭제될 회차"), leader.getId()).roundId();
			User third = userRepository.save(createUser("과거 참석자", 3));
			diningV2Service.applyAttendance(meetingId, deletedRound, third.getId());
			diningV2Service.deleteRound(meetingId, deletedRound, leader.getId());
			Integer newRound = diningV2Service.createRound(meetingId, roundBody("새 회차"), leader.getId()).roundId();
			diningV2Service.applyAttendance(meetingId, roundId, third.getId());

			diningNotificationSender.sendNewRoundNotification(meetingId, newRound);

			ArgumentCaptor<PushNotificationRequestDto> captor = ArgumentCaptor.forClass(
				PushNotificationRequestDto.class);
			org.mockito.Mockito.verify(pushNotificationService).sendPushNotification(captor.capture());
			assertThat(captor.getValue().getUserIds())
				.containsExactlyInAnyOrder(member.getId().toString(), third.getId().toString());
		}

		@Test
		@DisplayName("새 회차 알림은 다른 회식과 이후 회차의 삭제 이력을 포함하지 않는다.")
		void notification_excludesOtherDiningAndLaterRoundHistory() {
			Integer otherDiningId = createDining();
			Integer otherRoundId = getRounds(otherDiningId).get(0).getId();
			diningV2Service.applyAttendance(otherDiningId, otherRoundId, member.getId());
			diningV2Service.cancelAttendance(otherDiningId, otherRoundId, member.getId());
			Integer newRound = diningV2Service.createRound(meetingId, roundBody("2차"), leader.getId()).roundId();
			Integer laterRound = diningV2Service.createRound(meetingId, roundBody("3차"), leader.getId()).roundId();
			diningV2Service.applyAttendance(meetingId, laterRound, member.getId());
			diningV2Service.cancelAttendance(meetingId, laterRound, member.getId());

			diningNotificationSender.sendNewRoundNotification(meetingId, newRound);

			org.mockito.Mockito.verifyNoInteractions(pushNotificationService);
		}

		private void callAttendanceApi(String operation, Integer diningId, Integer targetRoundId) {
			switch (operation) {
				case "apply" -> diningV2Service.applyAttendance(diningId, targetRoundId, member.getId());
				case "cancel" -> diningV2Service.cancelAttendance(diningId, targetRoundId, member.getId());
				case "status" -> diningV2Service.getAttendance(diningId, targetRoundId, member.getId());
				case "modal" -> diningV2Service.getAttendanceAttendees(diningId, targetRoundId, member.getId());
				case "list" -> diningV2Service.getAttendees(diningId, targetRoundId, new DiningV2GetAttendeesQueryDto(null, null, null), leader.getId());
				case "add" -> diningV2Service.addAttendee(diningId, targetRoundId, member.getId(), leader.getId());
				case "delete" -> diningV2Service.deleteAttendee(diningId, targetRoundId, member.getId(), leader.getId());
				default -> throw new IllegalArgumentException(operation);
			}
		}
	}
}
