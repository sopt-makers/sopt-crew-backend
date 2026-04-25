package org.sopt.makers.crew.main.meeting.v2.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.comment.CommentRepository;
import org.sopt.makers.crew.main.entity.flash.FlashRepository;
import org.sopt.makers.crew.main.entity.like.LikeRepository;
import org.sopt.makers.crew.main.entity.meeting.CoLeaderReader;
import org.sopt.makers.crew.main.entity.meeting.CoLeaderRepository;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingReader;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.post.PostRepository;
import org.sopt.makers.crew.main.entity.tag.TagRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserFixture;
import org.sopt.makers.crew.main.entity.user.UserReader;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.external.s3.service.S3Service;
import org.sopt.makers.crew.main.flash.v2.dto.request.FlashV2CreateAndUpdateFlashBodyWithoutWelcomeMessageDto;
import org.sopt.makers.crew.main.global.config.ImageSettingProperties;
import org.sopt.makers.crew.main.global.util.ActiveGenerationProvider;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.meeting.v2.dto.ApplyMapper;
import org.sopt.makers.crew.main.meeting.v2.dto.FlashMeetingMapper;
import org.sopt.makers.crew.main.meeting.v2.dto.MeetingMapper;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2CreateAndUpdateMeetingForFlashResponseDto;
import org.sopt.makers.crew.main.tag.v2.service.TagV2Service;
import org.sopt.makers.crew.main.user.v2.service.lock.UserLockManager;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class MeetingV2ServiceFlashTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private ApplyRepository applyRepository;
	@Mock
	private MeetingRepository meetingRepository;
	@Mock
	private PostRepository postRepository;
	@Mock
	private CommentRepository commentRepository;
	@Mock
	private LikeRepository likeRepository;
	@Mock
	private CoLeaderRepository coLeaderRepository;
	@Mock
	private FlashRepository flashRepository;
	@Mock
	private TagRepository tagRepository;
	@Mock
	private MeetingReader meetingReader;
	@Mock
	private CoLeaderReader coLeaderReader;
	@Mock
	private ApplicationEventPublisher eventPublisher;
	@Mock
	private UserReader userReader;
	@Mock
	private S3Service s3Service;
	@Mock
	private TagV2Service tagV2Service;
	@Mock
	private UserLockManager userLockManager;
	@Mock
	private MeetingMapper meetingMapper;
	@Spy
	private FlashMeetingMapper flashMeetingMapper = Mappers.getMapper(FlashMeetingMapper.class);
	@Mock
	private ApplyMapper applyMapper;
	@Mock
	private ImageSettingProperties imageSettingProperties;
	@Mock
	private ActiveGenerationProvider activeGenerationProvider;
	@Mock
	private Time time;

	@InjectMocks
	private MeetingV2ServiceImpl meetingV2Service;

	private User user;
	private LocalDateTime fixedTime;

	@BeforeEach
	void setUp() {
		user = UserFixture.createStaticUser();
		user.setUserIdForTest(1);
		fixedTime = LocalDateTime.of(2024, 4, 24, 0, 0);
	}

	@Nested
	class 번쩍용_모임_생성 {

		@Test
		void 이미지가_비어있으면_기본_이미지를_넣고_flash_정책값으로_모임을_생성한다() {
			// given
			FlashV2CreateAndUpdateFlashBodyWithoutWelcomeMessageDto flashBody = createFlashBody(new ArrayList<>());

			doReturn(user).when(userRepository).findByIdOrThrow(1);
			doReturn("https://image.test/default-flash.png").when(imageSettingProperties).getDefaultFlashImage();
			doReturn(35).when(activeGenerationProvider).getActiveGeneration();
			doReturn(fixedTime).when(time).now();
			doAnswer(invocation -> {
				Meeting meeting = invocation.getArgument(0);
				setField(meeting, "id", 100);
				return meeting;
			}).when(meetingRepository).save(any(Meeting.class));

			// when
			MeetingV2CreateAndUpdateMeetingForFlashResponseDto response = meetingV2Service.createMeetingForFlash(1, flashBody);

			// then
			assertThat(response.meetingId()).isEqualTo(100);
			assertThat(response.files()).containsExactly("https://image.test/default-flash.png");

			org.mockito.ArgumentCaptor<Meeting> meetingCaptor = org.mockito.ArgumentCaptor.forClass(Meeting.class);
			verify(meetingRepository).save(meetingCaptor.capture());

			Meeting savedMeeting = meetingCaptor.getValue();
			assertThat(savedMeeting.getCategory()).isEqualTo(MeetingCategory.FLASH);
			assertThat(savedMeeting.getCreatedGeneration()).isEqualTo(35);
			assertThat(savedMeeting.getCanJoinOnlyActiveGeneration()).isFalse();
			assertThat(savedMeeting.getTargetActiveGeneration()).isNull();
			assertThat(savedMeeting.getStartDate()).isEqualTo(fixedTime);
			assertThat(savedMeeting.getEndDate()).isEqualTo(LocalDateTime.of(2024, 4, 24, 23, 59, 59));
			assertThat(savedMeeting.getmStartDate()).isEqualTo(LocalDateTime.of(2024, 4, 25, 0, 0));
			assertThat(savedMeeting.getmEndDate()).isEqualTo(LocalDateTime.of(2024, 4, 26, 23, 59, 59));
			assertThat(savedMeeting.getImageURL()).extracting("url")
				.containsExactly("https://image.test/default-flash.png");
			assertThat(savedMeeting.getJoinableParts()).containsExactly(MeetingJoinablePart.values());
		}
	}

	@Nested
	class 번쩍용_모임_수정 {

		@Test
		void 이미지가_비어있으면_기본_이미지를_넣고_flash_정책값으로_기존_모임을_수정한다() {
			// given
			Meeting existingMeeting = Meeting.builder()
				.user(user)
				.userId(user.getId())
				.title("기존 제목")
				.category(MeetingCategory.STUDY)
				.imageURL(List.of(new ImageUrlVO(0, "https://image.test/old.png")))
				.startDate(LocalDateTime.of(2024, 4, 20, 0, 0))
				.endDate(LocalDateTime.of(2024, 4, 21, 23, 59, 59))
				.capacity(3)
				.desc("기존 소개")
				.processDesc("기존 진행")
				.mStartDate(LocalDateTime.of(2024, 4, 22, 0, 0))
				.mEndDate(LocalDateTime.of(2024, 4, 23, 23, 59, 59))
				.leaderDesc("기존 리더")
				.note("기존 note")
				.isMentorNeeded(true)
				.canJoinOnlyActiveGeneration(true)
				.createdGeneration(35)
				.targetActiveGeneration(35)
				.joinableParts(new MeetingJoinablePart[] {MeetingJoinablePart.SERVER})
				.build();
			setField(existingMeeting, "id", 100);

			FlashV2CreateAndUpdateFlashBodyWithoutWelcomeMessageDto flashBody = createFlashBody(new ArrayList<>());

			doReturn(user).when(userRepository).findByIdOrThrow(1);
			doReturn("https://image.test/default-flash.png").when(imageSettingProperties).getDefaultFlashImage();
			doReturn(35).when(activeGenerationProvider).getActiveGeneration();
			doReturn(fixedTime).when(time).now();
			doReturn(Optional.of(existingMeeting)).when(meetingRepository).findById(100);

			// when
			MeetingV2CreateAndUpdateMeetingForFlashResponseDto response = meetingV2Service.updateMeetingForFlash(100, 1, flashBody);

			// then
			assertThat(response.meetingId()).isEqualTo(100);
			assertThat(response.files()).containsExactly("https://image.test/default-flash.png");

			assertThat(existingMeeting.getTitle()).isEqualTo("번쩍 제목");
			assertThat(existingMeeting.getCategory()).isEqualTo(MeetingCategory.FLASH);
			assertThat(existingMeeting.getCreatedGeneration()).isEqualTo(35);
			assertThat(existingMeeting.getCanJoinOnlyActiveGeneration()).isFalse();
			assertThat(existingMeeting.getTargetActiveGeneration()).isNull();
			assertThat(existingMeeting.getStartDate()).isEqualTo(fixedTime);
			assertThat(existingMeeting.getEndDate()).isEqualTo(LocalDateTime.of(2024, 4, 24, 23, 59, 59));
			assertThat(existingMeeting.getmStartDate()).isEqualTo(LocalDateTime.of(2024, 4, 25, 0, 0));
			assertThat(existingMeeting.getmEndDate()).isEqualTo(LocalDateTime.of(2024, 4, 26, 23, 59, 59));
			assertThat(existingMeeting.getProcessDesc()).isEmpty();
			assertThat(existingMeeting.getLeaderDesc()).isEmpty();
			assertThat(existingMeeting.getNote()).isEmpty();
			assertThat(existingMeeting.getImageURL()).extracting("url")
				.containsExactly("https://image.test/default-flash.png");
			assertThat(existingMeeting.getJoinableParts()).containsExactly(MeetingJoinablePart.values());

			verify(meetingRepository, never()).save(any(Meeting.class));
		}
	}

	private FlashV2CreateAndUpdateFlashBodyWithoutWelcomeMessageDto createFlashBody(ArrayList<String> files) {
		return new FlashV2CreateAndUpdateFlashBodyWithoutWelcomeMessageDto(
			"번쩍 제목",
			"번쩍 소개",
			"기간",
			"2024.04.25",
			"2024.04.26",
			"오프라인",
			"잠실역 5번 출구",
			1,
			5,
			files
		);
	}
}
