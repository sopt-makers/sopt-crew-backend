package org.sopt.makers.crew.main.flash.v2.service;

import static org.sopt.makers.crew.main.support.TestReflectionUtils.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.flash.Flash;
import org.sopt.makers.crew.main.entity.flash.FlashRepository;
import org.sopt.makers.crew.main.entity.flash.enums.FlashPlaceType;
import org.sopt.makers.crew.main.entity.flash.enums.FlashTimingType;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserFixture;
import org.sopt.makers.crew.main.entity.user.UserReader;
import org.sopt.makers.crew.main.external.notification.dto.event.KeywordEventDto;
import org.sopt.makers.crew.main.flash.v2.dto.mapper.FlashMapper;
import org.sopt.makers.crew.main.flash.v2.dto.request.FlashV2CreateAndUpdateFlashBodyDto;
import org.sopt.makers.crew.main.flash.v2.dto.request.FlashV2CreateAndUpdateFlashBodyWithoutWelcomeMessageDto;
import org.sopt.makers.crew.main.flash.v2.dto.response.FlashV2CreateResponseDto;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.util.ActiveGenerationProvider;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2CreateAndUpdateMeetingForFlashResponseDto;
import org.sopt.makers.crew.main.meeting.v2.service.MeetingV2Service;
import org.sopt.makers.crew.main.tag.v2.dto.response.TagV2CreateFlashTagResponseDto;
import org.sopt.makers.crew.main.tag.v2.service.TagV2Service;
import org.sopt.makers.crew.main.user.v2.service.UserV2Service;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class FlashV2ServiceTest {

	@Mock
	private UserV2Service userV2Service;
	@Mock
	private TagV2Service tagV2Service;
	@Mock
	private MeetingV2Service meetingV2Service;
	@Mock
	private FlashRepository flashRepository;
	@Mock
	private ApplyRepository applyRepository;
	@Mock
	private UserReader userReader;
	@Mock
	private ApplicationEventPublisher eventPublisher;
	@Mock
	private ActiveGenerationProvider activeGenerationProvider;
	@Mock
	private Time realTime;
	@Spy
	private FlashMapper flashMapper = Mappers.getMapper(FlashMapper.class);
	@InjectMocks
	private FlashV2ServiceImpl flashV2Service;
	private User user;
	private LocalDateTime fixedTime;

	@BeforeEach
	void setUp() {
		user = UserFixture.createStaticUser();
		user.setUserIdForTest(1);
		fixedTime = LocalDateTime.of(2024, 4, 24, 0, 0);
	}

	@Nested
	class 번쩍_생성 {

		@Test
		void 활동_정보가_없는_유저면_예외가_발생한다() {
			// given
			User invalidUser = User.builder()
				.name("활동 없음")
				.orgId(1)
				.activities(null)
				.phone("010-1111-1111")
				.build();
			FlashV2CreateAndUpdateFlashBodyDto request = createRequest(List.of("https://image.test/flash.png"));

			doReturn(invalidUser).when(userV2Service).getUserByUserId(1);

			// when & then
			assertThatThrownBy(() -> flashV2Service.createFlash(request, 1))
				.isInstanceOf(BadRequestException.class);

			verifyNoInteractions(meetingV2Service, flashRepository, tagV2Service, eventPublisher);
		}

		@Test
		void 소개_이미지가_한장을_초과하면_예외가_발생한다() {
			// given
			FlashV2CreateAndUpdateFlashBodyDto request = createRequest(
				List.of("https://image.test/one.png", "https://image.test/two.png"));

			doReturn(user).when(userV2Service).getUserByUserId(1);

			// when & then
			assertThatThrownBy(() -> flashV2Service.createFlash(request, 1))
				.isInstanceOf(BadRequestException.class);

			verifyNoInteractions(meetingV2Service, flashRepository, tagV2Service, eventPublisher);
		}

		@Test
		void 번쩍과_모임을_함께_생성하고_고정_시간에는_알림을_발행하지_않는다() {
			// given
			FlashV2CreateAndUpdateFlashBodyDto request = createRequest(List.of("https://image.test/flash.png"));
			MeetingV2CreateAndUpdateMeetingForFlashResponseDto meetingResponse =
				MeetingV2CreateAndUpdateMeetingForFlashResponseDto.of(10, request.flashBody());

			doReturn(user).when(userV2Service).getUserByUserId(1);
			doReturn(meetingResponse).when(meetingV2Service).createMeetingForFlash(1, request.flashBody());
			doReturn(35).when(activeGenerationProvider).getActiveGeneration();
			doReturn(fixedTime).when(realTime).now();
			doAnswer(invocation -> {
				Flash flash = invocation.getArgument(0);
				setField(flash, "id", 99);
				return flash;
			}).when(flashRepository).save(any(Flash.class));
			doReturn(TagV2CreateFlashTagResponseDto.from(20))
				.when(tagV2Service)
				.createFlashMeetingTag(request.welcomeMessageTypes(), request.meetingKeywordTypes(), 99, 10);

			// when
			FlashV2CreateResponseDto response = flashV2Service.createFlash(request, 1);

			// then
			assertThat(response.meetingId()).isEqualTo(10);
			assertThat(response.tagId()).isEqualTo(20);

			ArgumentCaptor<Flash> flashCaptor = ArgumentCaptor.forClass(Flash.class);
			verify(flashRepository).save(flashCaptor.capture());

			Flash savedFlash = flashCaptor.getValue();
			assertThat(savedFlash.getMeetingId()).isEqualTo(10);
			assertThat(savedFlash.getLeaderUserId()).isEqualTo(1);
			assertThat(savedFlash.getCreatedGeneration()).isEqualTo(35);
			assertThat(savedFlash.getStartDate()).isEqualTo(fixedTime);
			assertThat(savedFlash.getEndDate()).isEqualTo(LocalDateTime.of(2024, 4, 24, 23, 59, 59));
			assertThat(savedFlash.getActivityStartDate()).isEqualTo(LocalDateTime.of(2024, 4, 25, 0, 0));
			assertThat(savedFlash.getActivityEndDate()).isEqualTo(LocalDateTime.of(2024, 4, 26, 23, 59, 59));
			assertThat(savedFlash.getFlashTimingType()).isEqualTo(FlashTimingType.AFTER_DISCUSSION);
			assertThat(savedFlash.getFlashPlaceType()).isEqualTo(FlashPlaceType.OFFLINE);
			assertThat(savedFlash.getImageURL()).extracting("url")
				.containsExactly("https://image.test/flash.png");

			verify(eventPublisher, never()).publishEvent(any(KeywordEventDto.class));
			verifyNoInteractions(userReader);
		}
	}

	@Nested
	class 번쩍_수정 {

		@Test
		void 활동_정보가_없는_유저면_예외가_발생한다() {
			// given
			User invalidUser = User.builder()
				.name("활동 없음")
				.orgId(1)
				.activities(null)
				.phone("010-1111-1111")
				.build();
			FlashV2CreateAndUpdateFlashBodyDto request = createRequest(List.of("https://image.test/flash.png"));

			doReturn(invalidUser).when(userV2Service).getUserByUserId(1);
			doReturn(Optional.of(createExistingFlash())).when(flashRepository).findByMeetingId(10);

			// when & then
			assertThatThrownBy(() -> flashV2Service.updateFlash(10, request, 1))
				.isInstanceOf(BadRequestException.class);

			verifyNoInteractions(meetingV2Service, tagV2Service);
		}

		@Test
		void 소개_이미지가_한장을_초과하면_예외가_발생한다() {
			// given
			FlashV2CreateAndUpdateFlashBodyDto request = createRequest(
				List.of("https://image.test/one.png", "https://image.test/two.png"));

			doReturn(user).when(userV2Service).getUserByUserId(1);
			doReturn(Optional.of(createExistingFlash())).when(flashRepository).findByMeetingId(10);

			// when & then
			assertThatThrownBy(() -> flashV2Service.updateFlash(10, request, 1))
				.isInstanceOf(BadRequestException.class);

			verifyNoInteractions(meetingV2Service, tagV2Service);
		}

		@Test
		void 번쩍과_모임_정보를_함께_수정한다() {
			// given
			Flash existingFlash = createExistingFlash();
			FlashV2CreateAndUpdateFlashBodyDto request = new FlashV2CreateAndUpdateFlashBodyDto(
				new FlashV2CreateAndUpdateFlashBodyWithoutWelcomeMessageDto(
					"수정된 번쩍 제목",
					"수정된 번쩍 소개",
					"당일",
					"2024.04.27",
					"2024.04.28",
					"온라인",
					"디스코드",
					2,
					6,
					List.of("https://image.test/updated.png")
				),
				List.of("OB 환영"),
				List.of("먹방")
			);
			MeetingV2CreateAndUpdateMeetingForFlashResponseDto meetingResponse =
				MeetingV2CreateAndUpdateMeetingForFlashResponseDto.of(10, request.flashBody());

			doReturn(user).when(userV2Service).getUserByUserId(1);
			doReturn(Optional.of(existingFlash)).when(flashRepository).findByMeetingId(10);
			doReturn(meetingResponse).when(meetingV2Service).updateMeetingForFlash(10, 1, request.flashBody());
			doReturn(35).when(activeGenerationProvider).getActiveGeneration();
			doReturn(fixedTime).when(realTime).now();

			// when
			flashV2Service.updateFlash(10, request, 1);

			// then
			assertThat(existingFlash.getTitle()).isEqualTo("수정된 번쩍 제목");
			assertThat(existingFlash.getDesc()).isEqualTo("수정된 번쩍 소개");
			assertThat(existingFlash.getMeetingId()).isEqualTo(10);
			assertThat(existingFlash.getLeaderUserId()).isEqualTo(1);
			assertThat(existingFlash.getCreatedGeneration()).isEqualTo(35);
			assertThat(existingFlash.getStartDate()).isEqualTo(fixedTime);
			assertThat(existingFlash.getEndDate()).isEqualTo(LocalDateTime.of(2024, 4, 26, 23, 59, 59));
			assertThat(existingFlash.getActivityStartDate()).isEqualTo(LocalDateTime.of(2024, 4, 27, 0, 0));
			assertThat(existingFlash.getActivityEndDate()).isEqualTo(LocalDateTime.of(2024, 4, 28, 23, 59, 59));
			assertThat(existingFlash.getFlashTimingType()).isEqualTo(FlashTimingType.IMMEDIATE);
			assertThat(existingFlash.getFlashPlaceType()).isEqualTo(FlashPlaceType.ONLINE);
			assertThat(existingFlash.getImageURL()).extracting("url")
				.containsExactly("https://image.test/updated.png");

			verify(tagV2Service).updateFlashMeetingTag(
				request.welcomeMessageTypes(),
				request.meetingKeywordTypes(),
				existingFlash.getId()
			);
		}
	}

	private FlashV2CreateAndUpdateFlashBodyDto createRequest(List<String> files) {
		return new FlashV2CreateAndUpdateFlashBodyDto(
			new FlashV2CreateAndUpdateFlashBodyWithoutWelcomeMessageDto(
				"번쩍 제목",
				"번쩍 소개",
				"예정 기간 (협의 후 결정)",
				"2024.04.25",
				"2024.04.26",
				"오프라인",
				"잠실역 5번 출구",
				1,
				5,
				files
			),
			List.of("YB 환영"),
			List.of("운동")
		);
	}

	private Flash createExistingFlash() {
		Flash flash = Flash.builder()
			.leaderUserId(1)
			.meetingId(10)
			.title("기존 번쩍")
			.desc("기존 소개")
			.flashTimingType(FlashTimingType.AFTER_DISCUSSION)
			.startDate(LocalDateTime.of(2024, 4, 24, 0, 0))
			.endDate(LocalDateTime.of(2024, 4, 24, 23, 59, 59))
			.activityStartDate(LocalDateTime.of(2024, 4, 25, 0, 0))
			.activityEndDate(LocalDateTime.of(2024, 4, 26, 23, 59, 59))
			.flashPlaceType(FlashPlaceType.OFFLINE)
			.flashPlace("잠실역")
			.minimumCapacity(1)
			.maximumCapacity(5)
			.createdGeneration(34)
			.imageURL(List.of(new ImageUrlVO(0, "https://image.test/old.png")))
			.build();
		setField(flash, "id", 99);
		return flash;
	}
}
