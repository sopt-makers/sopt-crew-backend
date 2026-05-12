package org.sopt.makers.crew.main.advertisement.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.sopt.makers.crew.main.entity.advertisement.enums.AdvertisementCategory.*;
import static org.springframework.test.util.ReflectionTestUtils.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.admin.v2.dto.AdvertisementMeetingTopUpdateRequest;
import org.sopt.makers.crew.main.advertisement.dto.AdvertisementMeetingTopGetResponseDto;
import org.sopt.makers.crew.main.entity.advertisement.Advertisement;
import org.sopt.makers.crew.main.entity.advertisement.AdvertisementRepository;
import org.sopt.makers.crew.main.entity.advertisement.enums.AdvertisementCategory;
import org.sopt.makers.crew.main.entity.advertisement.enums.EventType;
import org.sopt.makers.crew.main.entity.advertisement.enums.TargetGeneration;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserFixture;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.util.ActiveGenerationProvider;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.meeting.v2.service.MeetingPartNormalizer;

@ExtendWith(MockitoExtension.class)
class AdvertisementServiceTest {

	private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 2, 10, 0);

	@Mock
	private AdvertisementRepository advertisementRepository;

	@Mock
	private MeetingRepository meetingRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ActiveGenerationProvider activeGenerationProvider;

	@Mock
	private Time time;

	private AdvertisementService advertisementService;
	private AdvertisementValidator advertisementValidator;
	private AdvertisementFactory advertisementFactory;

	@BeforeEach
	void setUp() {
		advertisementValidator = new AdvertisementValidator(advertisementRepository);
		advertisementFactory = new AdvertisementFactory(
			new MeetingPartNormalizer()
		);
		advertisementService = new AdvertisementService(
			advertisementRepository,
			meetingRepository,
			userRepository,
			activeGenerationProvider,
			new MeetingPartNormalizer(),
			time,
			advertisementValidator,
			advertisementFactory
		);
	}

	@Test
	@DisplayName("모임 상단 광고 조회 시 배너 정보와 리스트 진입 링크를 반환한다.")
	void getMeetingTopAdvertisement_returnsMeetingTopResponse() {
		User requestUser = UserFixture.createUser(1, "서버", 38);
		Advertisement advertisement = createAdvertisement(TargetGeneration.ALL);
		Meeting applicationMeeting = createMeeting(715);

		setField(advertisement, "id", 10);

		when(time.now()).thenReturn(NOW);
		when(activeGenerationProvider.getActiveGeneration()).thenReturn(38);
		when(userRepository.findByIdOrThrow(requestUser.getId())).thenReturn(requestUser);
		when(advertisementRepository.findMeetingTopAdvertisements(MEETING_TOP, NOW)).thenReturn(List.of(advertisement));
		when(meetingRepository.findFirstByTitleOrderByIdDesc("[38기 솝커톤] 서버 파트 신청"))
			.thenReturn(Optional.of(applicationMeeting));

		AdvertisementMeetingTopGetResponseDto response = advertisementService.getMeetingTopAdvertisement(
			requestUser.getId(), EventType.SOPKATHON);

		assertThat(response.isDisplay()).isTrue();
		assertThat(response.eventType()).isEqualTo(EventType.SOPKATHON);
		assertThat(response.advertisementId()).isEqualTo(10);
		assertThat(response.desktopImageUrl()).isEqualTo("https://example.com/desktop.png");
		assertThat(response.mobileImageUrl()).isEqualTo("https://example.com/mobile.png");
		assertThat(response.calendarImageUrl()).isEqualTo("https://example.com/calendar.png");
		assertThat(response.title().prefix()).isEqualTo("5월 4일 ");
		assertThat(response.title().highlight()).isEqualTo("솝커톤 신청");
		assertThat(response.title().suffix()).isEqualTo(" OPEN!");
		assertThat(response.subTitle()).isEqualTo("우리만의 해커톤, 누구보다 빠르게 신청하세요!");
		assertThat(response.bannerLink1()).isEqualTo("/list?search=38기+솝커톤&page=1");
		assertThat(response.bannerLink2()).isEqualTo("/detail?id=715");
	}

	@Test
	@DisplayName("모임 상단 광고 조회 시 요청 이벤트 타입의 텍스트로 상세 진입 링크를 반환한다.")
	void getMeetingTopAdvertisement_returnsLinksByRequestedEventType() {
		User requestUser = UserFixture.createUser(1, "서버", 38);
		Advertisement sopkathonAdvertisement = createAdvertisement(TargetGeneration.ALL, MEETING_TOP, true,
			EventType.SOPKATHON);
		Advertisement networkingAdvertisement = createAdvertisement(TargetGeneration.ALL, MEETING_TOP, true,
			EventType.NETWORKING);
		Meeting applicationMeeting = createMeeting(716);

		setField(networkingAdvertisement, "id", 11);

		when(time.now()).thenReturn(NOW);
		when(activeGenerationProvider.getActiveGeneration()).thenReturn(38);
		when(userRepository.findByIdOrThrow(requestUser.getId())).thenReturn(requestUser);
		when(advertisementRepository.findMeetingTopAdvertisements(MEETING_TOP, NOW)).thenReturn(
			List.of(sopkathonAdvertisement, networkingAdvertisement));
		when(meetingRepository.findFirstByTitleOrderByIdDesc("[38기 네트워킹] 서버 파트 신청"))
			.thenReturn(Optional.of(applicationMeeting));

		AdvertisementMeetingTopGetResponseDto response = advertisementService.getMeetingTopAdvertisement(
			requestUser.getId(), EventType.NETWORKING);

		assertThat(response.eventType()).isEqualTo(EventType.NETWORKING);
		assertThat(response.advertisementId()).isEqualTo(11);
		assertThat(response.bannerLink1()).isEqualTo("/list?search=38기+네트워킹&page=1");
		assertThat(response.bannerLink2()).isEqualTo("/detail?id=716");
	}

	@Test
	@DisplayName("파트 신청 모임이 없으면 배너를 노출하되 배너 링크 2는 반환하지 않는다.")
	void getMeetingTopAdvertisement_returnsNullBannerLink2WhenApplicationMeetingMissing() {
		User requestUser = UserFixture.createUser(1, "서버", 38);
		Advertisement advertisement = createAdvertisement(TargetGeneration.ALL);

		setField(advertisement, "id", 10);

		when(time.now()).thenReturn(NOW);
		when(activeGenerationProvider.getActiveGeneration()).thenReturn(38);
		when(userRepository.findByIdOrThrow(requestUser.getId())).thenReturn(requestUser);
		when(advertisementRepository.findMeetingTopAdvertisements(MEETING_TOP, NOW)).thenReturn(List.of(advertisement));
		when(meetingRepository.findFirstByTitleOrderByIdDesc("[38기 솝커톤] 서버 파트 신청"))
			.thenReturn(Optional.empty());

		AdvertisementMeetingTopGetResponseDto response = advertisementService.getMeetingTopAdvertisement(
			requestUser.getId(), EventType.SOPKATHON);

		assertThat(response.isDisplay()).isTrue();
		assertThat(response.bannerLink1()).isEqualTo("/list?search=38기+솝커톤&page=1");
		assertThat(response.bannerLink2()).isNull();
	}

	@Test
	@DisplayName("활동 기수 타겟 광고는 현재 활동 기수 활동이 없으면 노출하지 않는다.")
	void getMeetingTopAdvertisement_returnsNotDisplayWhenActiveGenerationTargetMisses() {
		User requestUser = UserFixture.createUser(1, "서버", 37);
		Advertisement advertisement = createAdvertisement(TargetGeneration.ACTIVE);

		when(time.now()).thenReturn(NOW);
		when(activeGenerationProvider.getActiveGeneration()).thenReturn(38);
		when(userRepository.findByIdOrThrow(requestUser.getId())).thenReturn(requestUser);
		when(advertisementRepository.findMeetingTopAdvertisements(MEETING_TOP, NOW)).thenReturn(List.of(advertisement));

		AdvertisementMeetingTopGetResponseDto response = advertisementService.getMeetingTopAdvertisement(
			requestUser.getId(), EventType.SOPKATHON);

		assertThat(response.isDisplay()).isFalse();
	}

	@Test
	@DisplayName("기존 광고 조회 API에서 MEETING_TOP 카테고리는 허용하지 않는다.")
	void getAdvertisement_rejectsMeetingTopCategory() {
		assertThatThrownBy(() -> advertisementService.getAdvertisement(MEETING_TOP))
			.isInstanceOf(BadRequestException.class);
	}

	@Test
	@DisplayName("어드민에서 모임 상단 광고 노출 여부를 변경한다.")
	void updateMeetingTopAdvertisementDisplay_updatesAdvertisementDisplay() {
		Advertisement advertisement = createAdvertisement(TargetGeneration.ALL, MEETING_TOP, true);
		setField(advertisement, "id", 10);

		doReturn(advertisement).when(advertisementRepository).findByIdOrThrow(10);

		Advertisement updatedAdvertisement = advertisementService.updateMeetingTopAdvertisementDisplay(10, false);

		assertThat(updatedAdvertisement.isDisplay()).isFalse();
	}

	// Todo : 기존 Meeting 카테고리의 노출 여부도 isDisplay로 관리하게 되면 삭제
	@Test
	@DisplayName("어드민 노출 여부 수정은 MEETING_TOP 광고만 허용한다.")
	void updateMeetingTopAdvertisementDisplay_rejectsGeneralAdvertisement() {
		Advertisement advertisement = createAdvertisement(TargetGeneration.ALL, MEETING, true);
		setField(advertisement, "id", 11);

		doReturn(advertisement).when(advertisementRepository).findByIdOrThrow(11);

		assertThatThrownBy(() -> advertisementService.updateMeetingTopAdvertisementDisplay(11, false))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("모임 상단 광고만 노출 여부를 수정할 수 있습니다.");
		assertThat(advertisement.isDisplay()).isTrue();
	}

	@Test
	@DisplayName("다른 모임 상단 광고가 이미 켜져 있으면 추가로 켤 수 없다.")
	void updateMeetingTopAdvertisementDisplay_rejectsWhenAnotherMeetingTopAdvertisementIsDisplayed() {
		Advertisement advertisement = createAdvertisement(TargetGeneration.ALL, MEETING_TOP, false);
		setField(advertisement, "id", 12);

		doReturn(advertisement).when(advertisementRepository).findByIdOrThrow(12);
		doReturn(true).when(advertisementRepository)
			.existsDisplayedAdvertisementByCategoryExcludingId(MEETING_TOP, 12);

		assertThatThrownBy(() -> advertisementService.updateMeetingTopAdvertisementDisplay(12, true))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("모임 상단 광고는 하나만 노출할 수 있습니다.");
		assertThat(advertisement.isDisplay()).isFalse();
	}

	@Test
	@DisplayName("어드민에서 모임 상단 광고 날짜와 이미지 URL을 부분 수정한다.")
	void updateMeetingTopAdvertisement_updatesDateAndImageFieldsPartially() {
		Advertisement advertisement = createAdvertisement(TargetGeneration.ALL, MEETING_TOP, true);
		setField(advertisement, "id", 13);
		LocalDateTime newStartDate = NOW.plusDays(2);
		LocalDateTime newEndDate = NOW.plusDays(5);
		AdvertisementMeetingTopUpdateRequest request = new AdvertisementMeetingTopUpdateRequest(
			null,
			newStartDate,
			newEndDate,
			"https://example.com/new-desktop.png",
			null,
			"https://example.com/new-calendar.png",
			null,
			null,
			null,
			null
		);

		doReturn(advertisement).when(advertisementRepository).findByIdOrThrow(13);

		Advertisement updatedAdvertisement = advertisementService.updateMeetingTopAdvertisement(13, request);

		assertThat(updatedAdvertisement.isDisplay()).isTrue();
		assertThat(updatedAdvertisement.getAdvertisementStartDate()).isEqualTo(newStartDate);
		assertThat(updatedAdvertisement.getAdvertisementEndDate()).isEqualTo(newEndDate);
		assertThat(updatedAdvertisement.getAdvertisementDesktopImageUrl()).isEqualTo("https://example.com/new-desktop.png");
		assertThat(updatedAdvertisement.getAdvertisementMobileImageUrl()).isEqualTo("https://example.com/mobile.png");
		assertThat(updatedAdvertisement.getCalendarImageUrl()).isEqualTo("https://example.com/new-calendar.png");
	}

	@Test
	@DisplayName("어드민 수정 시 이미지 URL을 빈 값으로 변경할 수 없다.")
	void updateMeetingTopAdvertisement_rejectsBlankImageUrl() {
		Advertisement advertisement = createAdvertisement(TargetGeneration.ALL, MEETING_TOP, true);
		setField(advertisement, "id", 14);
		AdvertisementMeetingTopUpdateRequest request = new AdvertisementMeetingTopUpdateRequest(
			null,
			null,
			null,
			" ",
			null,
			null,
			null,
			null,
			null,
			null
		);

		doReturn(advertisement).when(advertisementRepository).findByIdOrThrow(14);

		assertThatThrownBy(() -> advertisementService.updateMeetingTopAdvertisement(14, request))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("데스크톱 이미지 URL은 비워둘 수 없습니다.");
		assertThat(advertisement.getAdvertisementDesktopImageUrl()).isEqualTo("https://example.com/desktop.png");
	}

	@Test
	@DisplayName("어드민 수정 시 광고 시작일은 종료일보다 이후일 수 없다.")
	void updateMeetingTopAdvertisement_rejectsInvalidAdvertisementPeriod() {
		Advertisement advertisement = createAdvertisement(TargetGeneration.ALL, MEETING_TOP, true);
		setField(advertisement, "id", 15);
		AdvertisementMeetingTopUpdateRequest request = new AdvertisementMeetingTopUpdateRequest(
			null,
			NOW.plusDays(5),
			NOW.plusDays(2),
			null,
			null,
			null,
			null,
			null,
			null,
			null
		);

		doReturn(advertisement).when(advertisementRepository).findByIdOrThrow(15);

		assertThatThrownBy(() -> advertisementService.updateMeetingTopAdvertisement(15, request))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("광고 시작일은 종료일보다 이후일 수 없습니다.");
	}

	@Test
	@DisplayName("어드민 페이지에서 모임 상단 광고 전체 목록을 조회한다.")
	void getMeetingTopAdvertisementsForAdmin_returnsAllMeetingTopAdvertisements() {
		Advertisement displayedAdvertisement = createAdvertisement(TargetGeneration.ALL, MEETING_TOP, true);
		Advertisement hiddenAdvertisement = createAdvertisement(TargetGeneration.ACTIVE, MEETING_TOP, false);
		setField(displayedAdvertisement, "id", 16);
		setField(hiddenAdvertisement, "id", 17);

		doReturn(List.of(displayedAdvertisement, hiddenAdvertisement)).when(advertisementRepository)
			.findAdvertisementsByCategoryForAdmin(MEETING_TOP);

		assertThat(advertisementService.getMeetingTopAdvertisementsForAdmin())
			.extracting("advertisementId")
			.containsExactly(16, 17);
	}

	@Test
	@DisplayName("어드민에서 모임 상단 광고 제목과 부제목을 부분 수정한다.")
	void updateMeetingTopAdvertisement_updatesTitleFieldsPartially() {
		Advertisement advertisement = createAdvertisement(TargetGeneration.ALL, MEETING_TOP, true);
		setField(advertisement, "id", 18);
		AdvertisementMeetingTopUpdateRequest request = new AdvertisementMeetingTopUpdateRequest(
			null,
			null,
			null,
			null,
			null,
			null,
			"6월 1일 ",
			"네트워킹 신청",
			" START!",
			"새로운 사람들과 가볍게 연결돼요!"
		);

		doReturn(advertisement).when(advertisementRepository).findByIdOrThrow(18);

		Advertisement updatedAdvertisement = advertisementService.updateMeetingTopAdvertisement(18, request);

		assertThat(updatedAdvertisement.getTitlePrefix()).isEqualTo("6월 1일 ");
		assertThat(updatedAdvertisement.getTitleHighlight()).isEqualTo("네트워킹 신청");
		assertThat(updatedAdvertisement.getTitleSuffix()).isEqualTo(" START!");
		assertThat(updatedAdvertisement.getSubTitle()).isEqualTo("새로운 사람들과 가볍게 연결돼요!");
	}

	@Test
	@DisplayName("어드민 수정 시 제목과 부제목을 빈 값으로 변경할 수 없다.")
	void updateMeetingTopAdvertisement_rejectsBlankTitleFields() {
		Advertisement advertisement = createAdvertisement(TargetGeneration.ALL, MEETING_TOP, true);
		setField(advertisement, "id", 19);
		AdvertisementMeetingTopUpdateRequest request = new AdvertisementMeetingTopUpdateRequest(
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			" ",
			null,
			null
		);

		doReturn(advertisement).when(advertisementRepository).findByIdOrThrow(19);

		assertThatThrownBy(() -> advertisementService.updateMeetingTopAdvertisement(19, request))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("제목 highlight은 비워둘 수 없습니다.");
		assertThat(advertisement.getTitleHighlight()).isEqualTo("솝커톤 신청");
	}

	private Advertisement createAdvertisement(TargetGeneration targetGeneration) {
		return createAdvertisement(targetGeneration, MEETING_TOP, true);
	}

	private Advertisement createAdvertisement(TargetGeneration targetGeneration, AdvertisementCategory advertisementCategory,
		boolean isDisplay) {
		return createAdvertisement(targetGeneration, advertisementCategory, isDisplay, EventType.SOPKATHON);
	}

	private Advertisement createAdvertisement(TargetGeneration targetGeneration, AdvertisementCategory advertisementCategory,
		boolean isDisplay, EventType eventType) {
		return Advertisement.builder()
			.advertisementDesktopImageUrl("https://example.com/desktop.png")
			.advertisementMobileImageUrl("https://example.com/mobile.png")
			.advertisementLink("https://example.com")
			.calendarImageUrl("https://example.com/calendar.png")
			.titlePrefix("5월 4일 ")
			.titleHighlight("솝커톤 신청")
			.titleSuffix(" OPEN!")
			.subTitle("우리만의 해커톤, 누구보다 빠르게 신청하세요!")
			.advertisementCategory(advertisementCategory)
			.priority(1L)
			.advertisementStartDate(NOW.minusDays(1))
			.advertisementEndDate(NOW.plusDays(1))
			.isSponsoredContent(false)
			.isDisplay(isDisplay)
			.eventType(eventType)
			.targetGeneration(targetGeneration)
			.build();
	}

	private Meeting createMeeting(Integer id) {
		Meeting meeting = mock(Meeting.class);
		when(meeting.getId()).thenReturn(id);
		return meeting;
	}
}
