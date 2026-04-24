package org.sopt.makers.crew.main.advertisement.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.sopt.makers.crew.main.entity.advertisement.enums.AdvertisementCategory.*;
import static org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.advertisement.dto.AdvertisementMeetingTopGetResponseDto;
import org.sopt.makers.crew.main.entity.advertisement.Advertisement;
import org.sopt.makers.crew.main.entity.advertisement.AdvertisementRepository;
import org.sopt.makers.crew.main.entity.advertisement.enums.AdvertisementCategory;
import org.sopt.makers.crew.main.entity.advertisement.enums.EventType;
import org.sopt.makers.crew.main.entity.advertisement.enums.TargetGeneration;
import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingFrequency;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingType;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.meeting.vo.MeetingJoinInfo;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
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
	private ApplyRepository applyRepository;

	@Mock
	private ActiveGenerationProvider activeGenerationProvider;

	@Mock
	private Time time;

	private AdvertisementService advertisementService;

	@BeforeEach
	void setUp() {
		advertisementService = new AdvertisementService(
			advertisementRepository,
			meetingRepository,
			userRepository,
			applyRepository,
			activeGenerationProvider,
			new MeetingPartNormalizer(),
			time
		);
	}

	@Test
	@DisplayName("모임 상단 광고 조회 시 부제목과 참여 정보 관련 필드를 반환한다.")
	void getMeetingTopAdvertisement_returnsMeetingTopResponse() {
		User requestUser = createUser(1, "서버", 38);
		User participant = createUser(2, "백엔드", 38);
		Advertisement advertisement = createAdvertisement(TargetGeneration.ALL);
		MeetingJoinInfo joinInfo = new MeetingJoinInfo(MeetingType.ONLINE_OFFLINE, MeetingFrequency.LIGHT);
		Meeting meeting = createMeeting(joinInfo);
		Apply apply = Apply.builder()
			.meeting(meeting)
			.meetingId(100)
			.user(participant)
			.userId(participant.getId())
			.content("함께 참여하고 싶습니다.")
			.build();

		setField(advertisement, "id", 10);
		setField(meeting, "id", 100);

		when(time.now()).thenReturn(NOW);
		when(activeGenerationProvider.getActiveGeneration()).thenReturn(38);
		when(userRepository.findByIdOrThrow(requestUser.getId())).thenReturn(requestUser);
		when(advertisementRepository.findMeetingTopAdvertisements(MEETING_TOP, NOW)).thenReturn(List.of(advertisement));
		when(meetingRepository.findFirstByTitleOrderByIdDesc("[38기 솝커톤] 서버 파트 신청")).thenReturn(
			Optional.of(meeting));
		when(applyRepository.findAllByMeetingIdWithUser(100, List.of(WAITING, APPROVE), "ASC")).thenReturn(
			List.of(apply));

		AdvertisementMeetingTopGetResponseDto response = advertisementService.getMeetingTopAdvertisement(
			requestUser.getId());

		assertThat(response.isDisplay()).isTrue();
		assertThat(response.advertisementId()).isEqualTo(10);
		assertThat(response.meetingId()).isEqualTo(100);
		assertThat(response.title()).isEqualTo("[38기 솝커톤] 서버 파트 신청");
		assertThat(response.subTitle()).isEqualTo("서버 파트 솝커톤");
		assertThat(response.joinInfo()).isEqualTo(joinInfo);
		assertThat(response.participatingPartInfo().part()).isEqualTo("서버");
		assertThat(response.participatingPartInfo().participantCount()).isEqualTo(1);
		assertThat(response.browseAction().query()).isEqualTo("38기 솝커톤");
		assertThat(response.browseAction().page()).isEqualTo(1);
	}

	@Test
	@DisplayName("활동 기수 타겟 광고는 현재 활동 기수 활동이 없으면 노출하지 않는다.")
	void getMeetingTopAdvertisement_returnsNotDisplayWhenActiveGenerationTargetMisses() {
		User requestUser = createUser(1, "서버", 37);
		Advertisement advertisement = createAdvertisement(TargetGeneration.ACTIVE);

		when(time.now()).thenReturn(NOW);
		when(activeGenerationProvider.getActiveGeneration()).thenReturn(38);
		when(userRepository.findByIdOrThrow(requestUser.getId())).thenReturn(requestUser);
		when(advertisementRepository.findMeetingTopAdvertisements(MEETING_TOP, NOW)).thenReturn(List.of(advertisement));

		AdvertisementMeetingTopGetResponseDto response = advertisementService.getMeetingTopAdvertisement(
			requestUser.getId());

		assertThat(response.isDisplay()).isFalse();
		verify(meetingRepository, never()).findFirstByTitleOrderByIdDesc(anyString());
	}

	@Test
	@DisplayName("기존 광고 조회 API에서 MEETING_TOP 카테고리는 허용하지 않는다.")
	void getAdvertisement_rejectsMeetingTopCategory() {
		assertThatThrownBy(() -> advertisementService.getAdvertisement(MEETING_TOP))
			.isInstanceOf(BadRequestException.class);
	}

	@Test
	@DisplayName("어드민에서 모임 상단 광고 노출 여부를 변경한다.")
	void updateMeetingTopDisplay_updatesAdvertisementDisplay() {
		Advertisement advertisement = createAdvertisement(TargetGeneration.ALL, MEETING_TOP, true);
		setField(advertisement, "id", 10);

		doReturn(advertisement).when(advertisementRepository).findByIdOrThrow(10);

		Advertisement updatedAdvertisement = advertisementService.updateMeetingTopDisplay(10, false);

		assertThat(updatedAdvertisement.isDisplay()).isFalse();
	}

	@Test
	@DisplayName("다른 모임 상단 광고가 이미 켜져 있으면 추가로 켤 수 없다.")
	void updateMeetingTopDisplay_rejectsWhenAnotherMeetingTopAdvertisementIsDisplayed() {
		Advertisement advertisement = createAdvertisement(TargetGeneration.ALL, MEETING_TOP, false);
		setField(advertisement, "id", 12);

		doReturn(advertisement).when(advertisementRepository).findByIdOrThrow(12);
		doReturn(true).when(advertisementRepository)
			.existsDisplayedAdvertisementByCategoryExcludingId(MEETING_TOP, 12);

		assertThatThrownBy(() -> advertisementService.updateMeetingTopDisplay(12, true))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("모임 상단 광고는 하나만 노출할 수 있습니다.");
		assertThat(advertisement.isDisplay()).isFalse();
	}

	private Advertisement createAdvertisement(TargetGeneration targetGeneration) {
		return createAdvertisement(targetGeneration, MEETING_TOP, true);
	}

	private Advertisement createAdvertisement(TargetGeneration targetGeneration, AdvertisementCategory advertisementCategory,
		boolean isDisplay) {
		return Advertisement.builder()
			.advertisementDesktopImageUrl("https://example.com/desktop.png")
			.advertisementMobileImageUrl("https://example.com/mobile.png")
			.advertisementLink("https://example.com")
			.advertisementCategory(advertisementCategory)
			.priority(1L)
			.advertisementStartDate(NOW.minusDays(1))
			.advertisementEndDate(NOW.plusDays(1))
			.isSponsoredContent(false)
			.isDisplay(isDisplay)
			.eventType(EventType.SOPKATHON)
			.targetGeneration(targetGeneration)
			.build();
	}

	private Meeting createMeeting(MeetingJoinInfo joinInfo) {
		User leader = createUser(3, "서버", 38);
		return Meeting.builder()
			.user(leader)
			.userId(leader.getId())
			.title("[38기 솝커톤] 서버 파트 신청")
			.subTitle("서버 파트 솝커톤")
			.category(MeetingCategory.EVENT)
			.imageURL(List.of(new ImageUrlVO(1, "https://example.com/meeting.png")))
			.startDate(NOW.minusHours(1))
			.endDate(NOW.plusHours(1))
			.capacity(50)
			.desc("솝커톤 신청 모임입니다.")
			.processDesc("온오프라인으로 진행합니다.")
			.mStartDate(NOW.plusDays(1))
			.mEndDate(NOW.plusDays(2))
			.leaderDesc("운영진")
			.note("유의사항")
			.isMentorNeeded(false)
			.canJoinOnlyActiveGeneration(false)
			.joinInfo(joinInfo)
			.createdGeneration(38)
			.targetActiveGeneration(38)
			.joinableParts(MeetingJoinablePart.values())
			.build();
	}

	private User createUser(Integer userId, String part, int generation) {
		User user = User.builder()
			.orgId(userId)
			.name("테스트 유저")
			.activities(List.of(new UserActivityVO(part, generation)))
			.profileImage("https://example.com/profile.png")
			.phone("010-1234-5678")
			.build();
		user.setUserIdForTest(userId);
		return user;
	}

	private void setField(Object target, String fieldName, Object value) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (NoSuchFieldException | IllegalAccessException exception) {
			throw new IllegalStateException(exception);
		}
	}
}
