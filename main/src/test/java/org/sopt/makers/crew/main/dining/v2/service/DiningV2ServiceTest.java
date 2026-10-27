package org.sopt.makers.crew.main.dining.v2.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningPayerApprovedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningPayerRejectedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningPayerRequestedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningPayerRevokedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningRoundCreatedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.query.DiningV2GetAttendeesQueryDto;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateDiningBodyDto;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateRoundBodyDto;
import org.sopt.makers.crew.main.entity.dining.DiningAttendance;
import org.sopt.makers.crew.main.entity.dining.DiningAttendanceRepository;
import org.sopt.makers.crew.main.entity.dining.DiningDeletionHistory;
import org.sopt.makers.crew.main.entity.dining.DiningDeletionHistoryRepository;
import org.sopt.makers.crew.main.entity.dining.DiningRound;
import org.sopt.makers.crew.main.entity.dining.DiningRoundRepository;
import org.sopt.makers.crew.main.entity.dining.enums.DiningDeletionTargetType;
import org.sopt.makers.crew.main.entity.dining.enums.DiningPayerStatus;
import org.sopt.makers.crew.main.entity.dining.enums.DiningRoundStatus;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.external.CaffeineTestConfig;
import org.sopt.makers.crew.main.external.notification.PushNotificationService;
import org.sopt.makers.crew.main.external.notification.dto.request.PushNotificationRequestDto;
import org.sopt.makers.crew.main.external.s3.service.S3Service;
import org.sopt.makers.crew.main.global.annotation.IntegratedTest;
import org.sopt.makers.crew.main.global.config.ImageSettingProperties;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.ForbiddenException;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.sopt.makers.crew.main.global.exception.UnAuthorizedException;
import org.sopt.makers.crew.main.global.exception.ServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.util.ReflectionTestUtils;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

/**
 * StubTime 기준 현재 시각: 2024.04.24 00:00
 */
@IntegratedTest
@RecordApplicationEvents
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
	@SpyBean
	private DiningAttendanceRepository diningAttendanceRepository;
	@Autowired
	private DiningDeletionHistoryRepository diningDeletionHistoryRepository;
	@SpyBean
	private UserRepository userRepository;
	@Autowired
	private ImageSettingProperties imageSettingProperties;

	@Autowired
	private DiningNotificationSender diningNotificationSender;
	@MockBean
	private PushNotificationService pushNotificationService;
	@MockBean
	private S3Service s3Service;

	@Autowired
	private ApplicationEvents applicationEvents;

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
	class 참석자_CSV {
		private static final String CSV_URL = "https://image.test/dining-attendees.csv";
		private Integer meetingId;
		private Integer roundId;
		private Path uploadedFile;
		private byte[] uploadedBytes;

		@BeforeEach
		void createRoundAndCaptureUpload() {
			meetingId = createDining();
			roundId = getRounds(meetingId).get(0).getId();
			doAnswer(invocation -> {
				uploadedFile = Path.of(invocation.getArgument(0, String.class));
				uploadedBytes = Files.readAllBytes(uploadedFile);
				return CSV_URL;
			}).when(s3Service).uploadCSVFile(anyString());
		}

		private List<String[]> download(Integer requesterId) throws Exception {
			assertThat(diningV2Service.getAttendeesCsv(meetingId, roundId, requesterId).url()).isEqualTo(CSV_URL);
			assertThat(uploadedFile).doesNotExist();
			assertThat(uploadedFile.getParent().toRealPath()).isEqualTo(Path.of(System.getProperty("java.io.tmpdir")).toRealPath());
			assertThat(uploadedBytes).startsWith((byte)0xEF, (byte)0xBB, (byte)0xBF);
			String csv = new String(uploadedBytes, StandardCharsets.UTF_8);
			try (CSVReader reader = new CSVReaderBuilder(new StringReader(csv.substring(1)))
				.withKeepCarriageReturn(true).build()) {
				return reader.readAll();
			}
		}

		@Test
		@DisplayName("개설자는 정확한 네 컬럼의 UTF-8 BOM CSV를 내려받고, 연락처의 앞자리 0이 보존된다.")
		void leader_downloadsCsvWithHeaderAndTextPhone() throws Exception {
			ReflectionTestUtils.setField(leader, "phone", "01012345678");
			var rows = download(leader.getId());
			assertThat(rows).hasSize(2);
			assertThat(rows.get(0)).containsExactly("번호", "이름", "최근 기수", "연락처");
			assertThat(rows.get(1)).containsExactly("1", "개설자", "35", "\t01012345678");
		}

		@ParameterizedTest
		@ValueSource(booleans = {false, true})
		@DisplayName("다른 회차에서 승인된 공동 결제자도 선택 회차의 CSV를 받으며 종료 후에도 가능하다.")
		void approvedPayerInAnotherRound_canDownload(boolean ended) throws Exception {
			Integer secondRoundId = diningV2Service.createRound(meetingId, roundBody("2차"), leader.getId()).roundId();
			diningV2Service.applyAttendance(meetingId, secondRoundId, member.getId());
			diningV2Service.requestPayer(meetingId, secondRoundId, member.getId());
			diningV2Service.approvePayer(meetingId, secondRoundId, member.getId(), leader.getId());
			if (ended) {
				diningV2Service.endDining(meetingId, leader.getId());
			}
			assertThat(download(member.getId())).hasSize(2);
		}

		@ParameterizedTest
		@ValueSource(strings = {"none", "attendee", "pending", "revoked", "otherDining"})
		@DisplayName("미승인 참석자, 승인 해제자, 다른 회식의 결제자는 CSV 다운로드 권한이 없다.")
		void unauthorizedUser_isForbidden(String state) {
			if (!state.equals("none")) {
				Integer payerMeetingId = state.equals("otherDining") ? createDining() : meetingId;
				Integer payerRoundId = getRounds(payerMeetingId).get(0).getId();
				diningV2Service.applyAttendance(payerMeetingId, payerRoundId, member.getId());
				if (!state.equals("attendee")) {
					diningV2Service.requestPayer(payerMeetingId, payerRoundId, member.getId());
					if (!state.equals("pending")) {
						diningV2Service.approvePayer(payerMeetingId, payerRoundId, member.getId(), leader.getId());
						if (state.equals("revoked")) {
							diningV2Service.revokePayer(payerMeetingId, payerRoundId, member.getId(), leader.getId());
						}
					}
				}
			}
			assertThatThrownBy(() -> diningV2Service.getAttendeesCsv(meetingId, roundId, member.getId()))
				.isInstanceOf(ForbiddenException.class).hasMessageContaining(FORBIDDEN_EXCEPTION.getErrorCode());
			verifyNoInteractions(s3Service);
		}

		@Test
		@DisplayName("다른 회식의 회차는 권한 검사보다 먼저 404로 거절한다.")
		void roundFromAnotherDining_isNotFoundBeforePermissionCheck() {
			Integer otherRoundId = getRounds(createDining()).get(0).getId();
			assertThatThrownBy(() -> diningV2Service.getAttendeesCsv(meetingId, otherRoundId, member.getId()))
				.isInstanceOf(NotFoundException.class).hasMessageContaining(NOT_FOUND_DINING_ROUND.getErrorCode());
			verifyNoInteractions(s3Service);
		}

		@Test
		@DisplayName("선택한 회차의 현재 참석자만 포함하고 다른 회차 참석자와 삭제된 참석자는 제외한다.")
		void csv_containsOnlyCurrentAttendeesOfSelectedRound() throws Exception {
			Integer secondRoundId = diningV2Service.createRound(meetingId, roundBody("2차"), leader.getId()).roundId();
			diningV2Service.applyAttendance(meetingId, secondRoundId, member.getId());
			User deleted = userRepository.save(createUser("삭제 참석자", 3));
			diningV2Service.addAttendee(meetingId, roundId, deleted.getId(), leader.getId());
			diningV2Service.deleteAttendee(meetingId, roundId, deleted.getId(), leader.getId());
			assertThat(diningDeletionHistoryRepository.findAll()).hasSize(1);
			var rows = download(leader.getId());
			assertThat(rows).hasSize(2);
			assertThat(rows.get(1)[1]).isEqualTo("개설자");
		}

		@Test
		@DisplayName("화면이 10명씩 표시되어도 CSV에는 35명 전체가 포함되며 사용자 조회는 한 번이다.")
		void csv_exportsAll35AttendeesWithOneUserBatch() throws Exception {
			for (int i = 3; i < 37; i++) {
				User attendee = userRepository.save(createUser("참석자" + i, i));
				diningV2Service.addAttendee(meetingId, roundId, attendee.getId(), leader.getId());
			}
			var page = diningV2Service.getAttendees(meetingId, roundId,
				new DiningV2GetAttendeesQueryDto(1, 10, "desc"), leader.getId());
			assertThat(page.attendees()).hasSize(10);
			clearInvocations(userRepository, diningAttendanceRepository);
			var rows = download(leader.getId());
			assertThat(rows).hasSize(36);
			for (int i = 1; i <= 35; i++) {
				assertThat(rows.get(i)[0]).isEqualTo(String.valueOf(i));
			}
			verify(userRepository).findAllByIdInOrThrow(argThat(ids -> ids.size() == 35));
			verify(userRepository, never()).findByIdOrThrow(anyInt());
			verify(userRepository, never()).findById(anyInt());
			verify(diningAttendanceRepository, never()).findAllByRoundId(anyInt(), any(org.springframework.data.domain.Pageable.class));
		}

		@Test
		@DisplayName("등록 시각, 참석 ID 오름차순으로 정렬하여 1부터 번호를 부여하고 가장 최근 기수를 쓴다.")
		void csv_ordersByTimestampThenAttendanceId() throws Exception {
			User third = userRepository.save(User.builder().name("세 번째").orgId(3).phone("010-3333-3333")
				.activities(List.of(new UserActivityVO("서버", 36), new UserActivityVO("웹", 30))).build());
			diningV2Service.applyAttendance(meetingId, roundId, third.getId());
			diningV2Service.applyAttendance(meetingId, roundId, member.getId());
			for (var attendance : diningAttendanceRepository.findAllByRoundId(roundId)) {
				attendance.createdTimestamp = LocalDateTime.of(2024, 4, 24, 0, 0)
					.plusHours(attendance.getUserId().equals(leader.getId()) ? 1 : 0);
			}
			var rows = download(leader.getId());
			assertThat(rows.get(1)).containsExactly("1", "세 번째", "36", "\t010-3333-3333");
			assertThat(rows.get(2)).containsExactly("2", "참석자", "35", "\t010-1234-5678");
			assertThat(rows.get(3)).containsExactly("3", "개설자", "35", "\t010-1234-5678");
		}

		@Test
		@DisplayName("참석자가 없어도 헤더만 있는 CSV를 생성한다.")
		void emptyRound_exportsHeaderOnly() throws Exception {
			diningV2Service.deleteAttendee(meetingId, roundId, leader.getId(), leader.getId());
			clearInvocations(userRepository);
			var rows = download(leader.getId());
			assertThat(rows).hasSize(1);
			assertThat(rows.get(0)).containsExactly("번호", "이름", "최근 기수", "연락처");
			verify(userRepository, never()).findAllByIdInOrThrow(anyList());
		}

		@Test
		@DisplayName("개설자는 종료된 회식에서도 다운로드할 수 있다.")
		void endedDining_allowsLeaderDownload() throws Exception {
			diningV2Service.endDining(meetingId, leader.getId());
			assertThat(download(leader.getId())).hasSize(2);
		}

		@ParameterizedTest
		@ValueSource(booleans = {false, true})
		@DisplayName("활동 정보와 연락처가 없어도 빈 셀로 기록하며 CSV 특수문자는 보존한다.")
		void missingActivityAndPhone_exportsEmptyCells(boolean nullActivities) throws Exception {
			ReflectionTestUtils.setField(leader, "activities", nullActivities ? null : List.of());
			ReflectionTestUtils.setField(leader, "phone", null);
			ReflectionTestUtils.setField(leader, "name", "홍,\"길동\"\n참석자");
			assertThat(download(leader.getId()).get(1)).containsExactly("1", "홍,\"길동\"\n참석자", "", "");
		}

		@ParameterizedTest
		@ValueSource(strings = {"=1+1", "+1+1", "-1+1", "@SUM(1,1)", " =1+1", "\t=1+1", "\r=1+1",
			"\n=1+1", "\uFEFF=1+1", "\u00A0=1+1", "＝1+1", "＋1+1", "－1+1", "＠SUM(1,1)",
			"=1+1\",\"=2+2"})
		@DisplayName("사용자 입력의 수식 시작 문자와 선행 공백·제어문자를 텍스트로 처리한다.")
		void formulaLikeInput_isPrefixedInsideSingleCsvCell(String input) throws Exception {
			ReflectionTestUtils.setField(leader, "name", input);
			ReflectionTestUtils.setField(leader, "phone", input);
			var rows = download(leader.getId());
			assertThat(rows).hasSize(2);
			assertThat(rows.get(1)).containsExactly("1", "\t" + input, "35", "\t" + input);
		}

		@Test
		@DisplayName("S3 업로드 실패 시에도 임시 파일을 삭제한다.")
		void uploadFailure_deletesTemporaryFile() {
			doAnswer(invocation -> {
				uploadedFile = Path.of(invocation.getArgument(0, String.class));
				assertThat(uploadedFile).exists();
				throw new ServerException(S3_STORAGE_ERROR.getErrorCode());
			}).when(s3Service).uploadCSVFile(anyString());
			assertThatThrownBy(() -> diningV2Service.getAttendeesCsv(meetingId, roundId, leader.getId()))
				.isInstanceOf(ServerException.class).hasMessageContaining(S3_STORAGE_ERROR.getErrorCode());
			assertThat(uploadedFile).doesNotExist();
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
		@DisplayName("사전 조회 이후 중복이 발생해도 PostgreSQL unique 제약 위반은 중복 참석 예외로 변환한다.")
		void attendance_translatesDatabaseDuplicateAfterPrecheck() {
			// 동시 요청이 사전 중복 조회를 통과한 상황을 재현하고 실제 PostgreSQL 제약으로 검증한다.
			org.mockito.Mockito.doReturn(false).when(diningAttendanceRepository)
				.existsByRoundIdAndUserId(roundId, leader.getId());
			assertThatThrownBy(() -> diningV2Service.applyAttendance(meetingId, roundId, leader.getId()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(ALREADY_ATTENDED_DINING_ROUND.getErrorCode());
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
		@DisplayName("새 회차 생성 시 취소·삭제된 참석자와 개설자는 제외하고 현재 참석자만 알린다.")
		void notification_onlyIncludesCurrentAttendeesAtCreation() {
			diningV2Service.applyAttendance(meetingId, roundId, member.getId());
			diningV2Service.cancelAttendance(meetingId, roundId, member.getId());
			Integer deletedRound = diningV2Service.createRound(meetingId, roundBody("삭제될 회차"), leader.getId()).roundId();
			User removed = userRepository.save(createUser("과거 참석자", 3));
			diningV2Service.applyAttendance(meetingId, deletedRound, removed.getId());
			diningV2Service.deleteRound(meetingId, deletedRound, leader.getId());
			User current = userRepository.save(createUser("현재 참석자", 4));
			diningV2Service.applyAttendance(meetingId, roundId, current.getId());
			Integer newRound = diningV2Service.createRound(meetingId, roundBody("새 회차"), leader.getId()).roundId();
			DiningRoundCreatedEvent event = roundCreatedEvent(newRound);
			assertThat(event.attendeeUserIds()).containsExactly(current.getId());

			// 생성 이후 취소와 신규 신청이 있어도 생성 시점의 수신자는 유지된다.
			diningV2Service.cancelAttendance(meetingId, roundId, current.getId());
			diningV2Service.applyAttendance(meetingId, roundId, member.getId());
			diningNotificationSender.sendNewRoundNotification(event);

			ArgumentCaptor<PushNotificationRequestDto> captor = ArgumentCaptor.forClass(PushNotificationRequestDto.class);
			org.mockito.Mockito.verify(pushNotificationService).sendPushNotification(captor.capture());
			assertThat(captor.getValue().getUserIds()).containsExactly(current.getId().toString());
		}

		@Test
		@DisplayName("새 회차 알림은 여러 회차의 중복 참석자를 한 번만 포함하고 다른 회식은 제외한다.")
		void notification_deduplicatesCurrentAttendees() {
			Integer second = diningV2Service.createRound(meetingId, roundBody("2차"), leader.getId()).roundId();
			diningV2Service.applyAttendance(meetingId, roundId, member.getId());
			diningV2Service.applyAttendance(meetingId, second, member.getId());
			Integer otherDining = createDining();
			User other = userRepository.save(createUser("다른 회식 참석자", 3));
			diningV2Service.applyAttendance(otherDining, getRounds(otherDining).get(0).getId(), other.getId());
			Integer third = diningV2Service.createRound(meetingId, roundBody("3차"), leader.getId()).roundId();
			assertThat(roundCreatedEvent(third).attendeeUserIds()).containsExactly(member.getId());
			diningNotificationSender.sendNewRoundNotification(roundCreatedEvent(third));
			ArgumentCaptor<PushNotificationRequestDto> captor = ArgumentCaptor.forClass(PushNotificationRequestDto.class);
			org.mockito.Mockito.verify(pushNotificationService).sendPushNotification(captor.capture());
			assertThat(captor.getValue().getUserIds()).containsExactly(member.getId().toString());
		}

		@Test
		@DisplayName("생성 시 수신자가 없거나 발송 전에 회차가 삭제되면 알림을 보내지 않는다.")
		void notification_skipsEmptyRecipientsAndDeletedRound() {
			Integer second = diningV2Service.createRound(meetingId, roundBody("2차"), leader.getId()).roundId();
			diningV2Service.applyAttendance(meetingId, roundId, member.getId());
			diningNotificationSender.sendNewRoundNotification(roundCreatedEvent(second));
			Integer third = diningV2Service.createRound(meetingId, roundBody("3차"), leader.getId()).roundId();
			diningV2Service.deleteRound(meetingId, third, leader.getId());
			diningNotificationSender.sendNewRoundNotification(roundCreatedEvent(third));
			org.mockito.Mockito.verifyNoInteractions(pushNotificationService);
		}

		private DiningRoundCreatedEvent roundCreatedEvent(Integer roundId) {
			return applicationEvents.stream(DiningRoundCreatedEvent.class)
				.filter(event -> event.roundId().equals(roundId)).findFirst().orElseThrow();
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

	@Nested
	class 공동_결제자 {
		private Integer meetingId;
		private Integer roundId;

		@BeforeEach
		void prepareDining() {
			meetingId = createDining();
			roundId = getRounds(meetingId).get(0).getId();
			diningV2Service.applyAttendance(meetingId, roundId, member.getId());
		}

		private DiningAttendance attendance() {
			return diningAttendanceRepository.findByRoundIdAndUserId(roundId, member.getId()).orElseThrow();
		}

		private void request() {
			diningV2Service.requestPayer(meetingId, roundId, member.getId());
		}

		private void approve() {
			diningV2Service.approvePayer(meetingId, roundId, member.getId(), leader.getId());
		}

		private void change(String action, Integer diningId, Integer targetRoundId, Integer actor) {
			switch (action) {
				case "approve" -> diningV2Service.approvePayer(diningId, targetRoundId, member.getId(), actor);
				case "reject" -> diningV2Service.rejectPayer(diningId, targetRoundId, member.getId(), actor);
				case "revoke" -> diningV2Service.revokePayer(diningId, targetRoundId, member.getId(), actor);
				default -> throw new IllegalArgumentException(action);
			}
		}

		@ParameterizedTest
		@ValueSource(booleans = {false, true})
		void requestAndApprove_workOnOpenAndClosedRounds(boolean closed) {
			if (closed) {
				diningV2Service.closeRound(meetingId, roundId, leader.getId());
			}
			request();
			assertThat(attendance().getPayerStatus()).isEqualTo(DiningPayerStatus.PENDING);
			assertThat(attendance().getPayerRequestedAt()).isEqualTo(LocalDateTime.of(2024, 4, 24, 0, 0));
			var event = applicationEvents.stream(DiningPayerRequestedEvent.class).findFirst().orElseThrow();
			assertThat(event.meetingId()).isEqualTo(meetingId);
			assertThat(event.roundId()).isEqualTo(roundId);
			assertThat(event.requesterUserId()).isEqualTo(member.getId());
			assertThat(event.leaderUserId()).isEqualTo(leader.getId());
			assertThat(event.requesterName()).isEqualTo(member.getName());
			assertThat(event.roundNumber()).isEqualTo(1);
			approve();
			assertThat(attendance().isApprovedPayer()).isTrue();
			assertThat(applicationEvents.stream(DiningPayerApprovedEvent.class)).hasSize(1);
		}

		@Test
		void request_requiresAttendanceAndRejectsLeader() {
			diningV2Service.cancelAttendance(meetingId, roundId, member.getId());
			assertThatThrownBy(this::request).isInstanceOf(NotFoundException.class)
				.hasMessageContaining(NOT_FOUND_DINING_ATTENDANCE.getErrorCode());
			assertThatThrownBy(() -> diningV2Service.requestPayer(meetingId, roundId, leader.getId()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(CANNOT_REQUEST_DINING_PAYER_AS_LEADER.getErrorCode());
			assertThat(applicationEvents.stream(DiningPayerRequestedEvent.class)).isEmpty();
		}

		@ParameterizedTest
		@EnumSource(DiningPayerStatus.class)
		void duplicateRequest_isRejected(DiningPayerStatus status) {
			request();
			if (status == DiningPayerStatus.APPROVED) { approve(); }
			assertThatThrownBy(this::request).isInstanceOf(BadRequestException.class)
				.hasMessageContaining(ALREADY_REQUESTED_DINING_PAYER.getErrorCode());
			assertThat(applicationEvents.stream(DiningPayerRequestedEvent.class)).hasSize(1);
		}

		@Test
		void requestOnAnotherRound_isIndependentOfApproval() {
			request();
			approve();
			Integer second = diningV2Service.createRound(meetingId, roundBody("2차"), leader.getId()).roundId();
			diningV2Service.applyAttendance(meetingId, second, member.getId());
			diningV2Service.requestPayer(meetingId, second, member.getId());
			assertThat(attendance().isApprovedPayer()).isTrue();
			assertThat(diningAttendanceRepository.findByRoundIdAndUserId(second, member.getId()).orElseThrow()
				.getPayerStatus()).isEqualTo(DiningPayerStatus.PENDING);
		}

		@ParameterizedTest
		@ValueSource(strings = {"request", "approve", "reject", "revoke"})
		void crossDiningRound_isRejected(String action) {
			Integer otherDining = createDining();
			assertThatThrownBy(() -> {
				if (action.equals("request")) {
					diningV2Service.requestPayer(otherDining, roundId, member.getId());
				} else {
					change(action, otherDining, roundId, leader.getId());
				}
			}).isInstanceOf(NotFoundException.class).hasMessageContaining(NOT_FOUND_DINING_ROUND.getErrorCode());
			assertThat(attendance().getPayerStatus()).isNull();
		}

		@ParameterizedTest
		@ValueSource(booleans = {false, true})
		void nonLeader_cannotViewOrProcessRequests_evenIfApproved(boolean approved) {
			request();
			if (approved) { approve(); }
			assertThatThrownBy(() -> diningV2Service.getPayerRequests(meetingId, member.getId()))
				.isInstanceOf(ForbiddenException.class);
			for (String action : List.of("approve", "reject", "revoke")) {
				assertThatThrownBy(() -> change(action, meetingId, roundId, member.getId()))
					.isInstanceOf(ForbiddenException.class);
			}
		}

		@ParameterizedTest
		@ValueSource(strings = {"reject", "revoke"})
		void clearOnClosedRound_preservesAttendanceAndAllowsNewRequest(String action) {
			request();
			if (action.equals("revoke")) { approve(); }
			Integer attendanceId = attendance().getId();
			diningV2Service.closeRound(meetingId, roundId, leader.getId());
			change(action, meetingId, roundId, leader.getId());
			assertThat(attendance().getId()).isEqualTo(attendanceId);
			assertThat(attendance().getPayerStatus()).isNull();
			assertThat(attendance().getPayerRequestedAt()).isNull();
			if (action.equals("reject")) {
				assertThat(applicationEvents.stream(DiningPayerRejectedEvent.class)).hasSize(1);
			} else {
				assertThat(applicationEvents.stream(DiningPayerRevokedEvent.class)).hasSize(1);
			}
			request();
			assertThat(attendance().getPayerStatus()).isEqualTo(DiningPayerStatus.PENDING);
		}

		@ParameterizedTest
		@NullSource
		@EnumSource(DiningPayerStatus.class)
		void onlyValidSourceStates_canBeProcessed(DiningPayerStatus state) {
			if (state != null) { request(); }
			if (state == DiningPayerStatus.APPROVED) { approve(); }
			if (state != DiningPayerStatus.PENDING) {
				for (String action : List.of("approve", "reject")) {
					assertThatThrownBy(() -> change(action, meetingId, roundId, leader.getId()))
						.isInstanceOf(BadRequestException.class).hasMessageContaining(NOT_PENDING_DINING_PAYER.getErrorCode());
				}
			}
			if (state != DiningPayerStatus.APPROVED) {
				assertThatThrownBy(() -> change("revoke", meetingId, roundId, leader.getId()))
					.isInstanceOf(BadRequestException.class).hasMessageContaining(NOT_APPROVED_DINING_PAYER.getErrorCode());
			}
		}

		@Test
		void requestsList_returnsPendingProfilesAndCurrentRoundNumbers() {
			assertThat(diningV2Service.getPayerRequests(meetingId, leader.getId()).requests()).isEmpty();
			Integer second = diningV2Service.createRound(meetingId, roundBody("2차"), leader.getId()).roundId();
			Integer third = diningV2Service.createRound(meetingId, roundBody("3차"), leader.getId()).roundId();
			diningV2Service.applyAttendance(meetingId, third, member.getId());
			request();
			diningV2Service.requestPayer(meetingId, third, member.getId());
			diningV2Service.deleteRound(meetingId, second, leader.getId());
			var requests = diningV2Service.getPayerRequests(meetingId, leader.getId()).requests();
			assertThat(requests).hasSize(2);
			assertThat(requests).extracting(r -> r.userId()).containsExactly(member.getId(), member.getId());
			assertThat(requests).extracting(r -> r.roundNumber()).containsExactly(1, 2);
			assertThat(requests.get(1).roundId()).isEqualTo(third);
			assertThat(requests.get(0).name()).isEqualTo(member.getName());
			assertThat(requests.get(0).profileImage()).isEqualTo(member.getProfileImage());
			assertThat(requests.get(0).requestedAt()).isEqualTo(attendance().getPayerRequestedAt());
			approve();
			assertThat(diningV2Service.getPayerRequests(meetingId, leader.getId()).requests()).hasSize(1);
			diningV2Service.rejectPayer(meetingId, third, member.getId(), leader.getId());
			assertThat(diningV2Service.getPayerRequests(meetingId, leader.getId()).requests()).isEmpty();
		}

		@Test
		void attendanceResponse_exposesCurrentUsersPayerState() {
			assertThat(diningV2Service.getAttendance(meetingId, roundId, member.getId()).payerStatus()).isNull();
			request();
			assertThat(diningV2Service.getAttendance(meetingId, roundId, member.getId()).payerStatus())
				.isEqualTo(DiningPayerStatus.PENDING);
			assertThat(diningV2Service.getAttendance(meetingId, roundId, leader.getId()).payerStatus()).isNull();
			approve();
			assertThat(diningV2Service.getAttendance(meetingId, roundId, member.getId()).payerStatus())
				.isEqualTo(DiningPayerStatus.APPROVED);
			diningV2Service.revokePayer(meetingId, roundId, member.getId(), leader.getId());
			assertThat(diningV2Service.getAttendance(meetingId, roundId, member.getId()).payerStatus()).isNull();
			diningV2Service.cancelAttendance(meetingId, roundId, member.getId());
			var absent = diningV2Service.getAttendance(meetingId, roundId, member.getId());
			assertThat(absent.isAttending()).isFalse();
			assertThat(absent.payerStatus()).isNull();
		}

		@ParameterizedTest
		@ValueSource(booleans = {false, true})
		void deletingAttendance_keepsPermissionOnlyIfAnotherApprovalRemains(boolean anotherApproval) {
			request();
			approve();
			Integer second = diningV2Service.createRound(meetingId, roundBody("2차"), leader.getId()).roundId();
			diningV2Service.applyAttendance(meetingId, second, member.getId());
			if (anotherApproval) {
				diningV2Service.requestPayer(meetingId, second, member.getId());
				diningV2Service.approvePayer(meetingId, second, member.getId(), leader.getId());
			}
			assertThat(diningV2Service.getAttendees(meetingId, second, new DiningV2GetAttendeesQueryDto(1, 10, "desc"), member.getId()))
				.isNotNull();
			diningV2Service.deleteAttendee(meetingId, roundId, member.getId(), member.getId());
			assertThat(diningAttendanceRepository.findByRoundIdAndUserId(roundId, member.getId())).isEmpty();
			assertThat(diningDeletionHistoryRepository.findAll()).anyMatch(h -> h.getDeletedByUserId().equals(member.getId()));
			if (anotherApproval) {
				assertThat(diningV2Service.getAttendees(meetingId, second, new DiningV2GetAttendeesQueryDto(1, 10, "desc"), member.getId()))
					.isNotNull();
			} else {
				assertThatThrownBy(() -> diningV2Service.getAttendees(meetingId, second,
					new DiningV2GetAttendeesQueryDto(1, 10, "desc"), member.getId())).isInstanceOf(ForbiddenException.class);
			}
		}

		@ParameterizedTest
		@CsvSource({"cancel,false", "cancel,true", "round,false", "round,true"})
		void attendanceOrRoundDeletion_updatesMeetingPermission(String deletion, boolean anotherApproval) {
			request();
			if (anotherApproval) { approve(); }
			Integer second = diningV2Service.createRound(meetingId, roundBody("2차"), leader.getId()).roundId();
			diningV2Service.applyAttendance(meetingId, second, member.getId());
			diningV2Service.requestPayer(meetingId, second, member.getId());
			diningV2Service.approvePayer(meetingId, second, member.getId(), leader.getId());
			if (deletion.equals("cancel")) {
				diningV2Service.cancelAttendance(meetingId, second, member.getId());
			} else {
				diningV2Service.deleteRound(meetingId, second, leader.getId());
			}
			assertThat(diningAttendanceRepository.findByRoundIdAndUserId(second, member.getId())).isEmpty();
			assertThat(diningAttendanceRepository.existsByMeetingIdAndUserIdAndPayerStatus(
				meetingId, member.getId(), DiningPayerStatus.APPROVED)).isEqualTo(anotherApproval);
		}

		@ParameterizedTest
		@ValueSource(strings = {"cancel", "round", "management"})
		void deletingPendingAttendance_removesRequest(String deletion) {
			Integer second = diningV2Service.createRound(meetingId, roundBody("2차"), leader.getId()).roundId();
			diningV2Service.applyAttendance(meetingId, second, member.getId());
			diningV2Service.requestPayer(meetingId, second, member.getId());
			switch (deletion) {
				case "cancel" -> diningV2Service.cancelAttendance(meetingId, second, member.getId());
				case "round" -> diningV2Service.deleteRound(meetingId, second, leader.getId());
				case "management" -> diningV2Service.deleteAttendee(meetingId, second, member.getId(), leader.getId());
			}
			assertThat(diningAttendanceRepository.findByRoundIdAndUserId(second, member.getId())).isEmpty();
			assertThat(diningV2Service.getPayerRequests(meetingId, leader.getId()).requests()).isEmpty();
		}

		@ParameterizedTest
		@ValueSource(strings = {"approve", "reject", "revoke"})
		void processingRequiresAttendanceInTargetRound(String action) {
			Integer second = diningV2Service.createRound(meetingId, roundBody("2차"), leader.getId()).roundId();
			request();
			assertThatThrownBy(() -> change(action, meetingId, second, leader.getId()))
				.isInstanceOf(NotFoundException.class).hasMessageContaining(NOT_FOUND_DINING_ATTENDANCE.getErrorCode());
			assertThat(attendance().getPayerStatus()).isEqualTo(DiningPayerStatus.PENDING);
		}

		@Test
		void nonDiningMeeting_isRejectedByAllPayerOperations() {
			ReflectionTestUtils.setField(meetingRepository.findDiningByIdOrThrow(meetingId), "category", MeetingCategory.STUDY);
			assertThatThrownBy(this::request).isInstanceOf(NotFoundException.class)
				.hasMessageContaining(NOT_FOUND_DINING.getErrorCode());
			assertThatThrownBy(() -> diningV2Service.getPayerRequests(meetingId, leader.getId()))
				.isInstanceOf(NotFoundException.class).hasMessageContaining(NOT_FOUND_DINING.getErrorCode());
			for (String action : List.of("approve", "reject", "revoke")) {
				assertThatThrownBy(() -> change(action, meetingId, roundId, leader.getId()))
					.isInstanceOf(NotFoundException.class).hasMessageContaining(NOT_FOUND_DINING.getErrorCode());
			}
		}

		@Test
		void endedDining_allowsPayerRequest() {
			diningV2Service.endDining(meetingId, leader.getId());
			assertThat(getRounds(meetingId).get(0).isClosed()).isTrue();

			request();

			assertThat(attendance().getPayerStatus()).isEqualTo(DiningPayerStatus.PENDING);
			assertThat(attendance().getPayerRequestedAt()).isEqualTo(LocalDateTime.of(2024, 4, 24, 0, 0));
			assertThat(applicationEvents.stream(DiningPayerRequestedEvent.class)).hasSize(1);
		}

		@Test
		void endedDining_allowsPendingPayerApproval() {
			request();
			diningV2Service.endDining(meetingId, leader.getId());

			approve();

			assertThat(attendance().isApprovedPayer()).isTrue();
			assertThat(applicationEvents.stream(DiningPayerApprovedEvent.class)).hasSize(1);
			assertThat(diningV2Service.getAttendees(meetingId, roundId,
				new DiningV2GetAttendeesQueryDto(1, 10, "desc"), member.getId()).attendees()).hasSize(2);
		}

		@Test
		void endedDining_allowsPendingPayerRejection() {
			request();
			Integer attendanceId = attendance().getId();
			diningV2Service.endDining(meetingId, leader.getId());

			diningV2Service.rejectPayer(meetingId, roundId, member.getId(), leader.getId());

			assertThat(attendance().getId()).isEqualTo(attendanceId);
			assertThat(attendance().getPayerStatus()).isNull();
			assertThat(attendance().getPayerRequestedAt()).isNull();
			assertThat(applicationEvents.stream(DiningPayerRejectedEvent.class)).hasSize(1);
		}

		@Test
		void endedDining_allowsApprovedPayerRevocation() {
			request();
			approve();
			Integer attendanceId = attendance().getId();
			diningV2Service.endDining(meetingId, leader.getId());
			assertThat(diningV2Service.getAttendees(meetingId, roundId,
				new DiningV2GetAttendeesQueryDto(1, 10, "desc"), member.getId()).attendees()).hasSize(2);

			diningV2Service.revokePayer(meetingId, roundId, member.getId(), leader.getId());

			assertThat(attendance().getId()).isEqualTo(attendanceId);
			assertThat(attendance().getPayerStatus()).isNull();
			assertThat(attendance().getPayerRequestedAt()).isNull();
			assertThat(applicationEvents.stream(DiningPayerRevokedEvent.class)).hasSize(1);
		}

		@ParameterizedTest
		@ValueSource(booleans = {false, true})
		void revokingOneRound_keepsOtherApprovalAndMeetingManagement(boolean ended) {
			request();
			approve();
			Integer second = diningV2Service.createRound(meetingId, roundBody("2차"), leader.getId()).roundId();
			diningV2Service.applyAttendance(meetingId, second, member.getId());
			diningV2Service.requestPayer(meetingId, second, member.getId());
			diningV2Service.approvePayer(meetingId, second, member.getId(), leader.getId());
			if (ended) {
				diningV2Service.endDining(meetingId, leader.getId());
			}
			Integer attendanceId = attendance().getId();

			diningV2Service.revokePayer(meetingId, roundId, member.getId(), leader.getId());

			assertThat(attendance().getId()).isEqualTo(attendanceId);
			assertThat(attendance().getPayerStatus()).isNull();
			assertThat(attendance().getPayerRequestedAt()).isNull();
			assertThat(diningAttendanceRepository.findByRoundIdAndUserId(second, member.getId()).orElseThrow()
				.isApprovedPayer()).isTrue();
			assertThat(diningV2Service.getAttendees(meetingId, roundId,
				new DiningV2GetAttendeesQueryDto(1, 10, "desc"), member.getId()).attendees()).hasSize(2);
			User third = userRepository.save(createUser("추가 참석자", 3));
			diningV2Service.addAttendee(meetingId, roundId, third.getId(), member.getId());
			assertThat(diningAttendanceRepository.existsByRoundIdAndUserId(roundId, third.getId())).isTrue();
			diningV2Service.deleteAttendee(meetingId, roundId, third.getId(), member.getId());
			assertThat(diningAttendanceRepository.existsByRoundIdAndUserId(roundId, third.getId())).isFalse();
		}

		@Test
		void revokeEvent_snapshotsCurrentRoundNumberAfterMiddleRoundDeletion() {
			Integer second = diningV2Service.createRound(meetingId, roundBody("2차"), leader.getId()).roundId();
			Integer third = diningV2Service.createRound(meetingId, roundBody("3차"), leader.getId()).roundId();
			diningV2Service.applyAttendance(meetingId, third, member.getId());
			diningV2Service.requestPayer(meetingId, third, member.getId());
			diningV2Service.approvePayer(meetingId, third, member.getId(), leader.getId());
			diningV2Service.deleteRound(meetingId, second, leader.getId());

			diningV2Service.revokePayer(meetingId, third, member.getId(), leader.getId());

			var event = applicationEvents.stream(DiningPayerRevokedEvent.class).findFirst().orElseThrow();
			assertThat(event.meetingId()).isEqualTo(meetingId);
			assertThat(event.roundId()).isEqualTo(third);
			assertThat(event.requesterUserId()).isEqualTo(member.getId());
			assertThat(event.diningTitle()).isEqualTo("서버 파트 회식");
			assertThat(event.roundNumber()).isEqualTo(2);
			diningV2Service.deleteRound(meetingId, third, leader.getId());
			assertThat(event.roundNumber()).isEqualTo(2);
		}

	}
}
