package org.sopt.makers.crew.main.meetingdemand.v2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingFrequency;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingType;
import org.sopt.makers.crew.main.entity.meeting.vo.MeetingJoinInfo;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemand;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandRepository;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandWait;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandWaitRepository;
import org.sopt.makers.crew.main.entity.meetingdemand.enums.MeetingDemandStatus;
import org.sopt.makers.crew.main.entity.report.ReportRepository;
import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserFixture;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.request.MeetingDemandV2CreateMeetingDemandBodyDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2CreateMeetingDemandResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2SwitchMeetingDemandWaitResponseDto;

@ExtendWith(MockitoExtension.class)
class MeetingDemandV2ServiceTest {

	private static final int MEETING_DEMAND_ID = 10;
	private static final int WRITER_ID = 1;
	private static final int REQUEST_USER_ID = 2;

	@Mock
	private UserRepository userRepository;

	@Mock
	private MeetingDemandRepository meetingDemandRepository;

	@Mock
	private MeetingDemandWaitRepository meetingDemandWaitRepository;

	@Mock
	private MeetingRepository meetingRepository;

	@Mock
	private ReportRepository reportRepository;

	private MeetingDemandV2ServiceImpl meetingDemandV2Service;

	private User writer;
	private MeetingDemand meetingDemand;

	@BeforeEach
	void setUp() {
		meetingDemandV2Service = new MeetingDemandV2ServiceImpl(
			userRepository,
			meetingDemandRepository,
			meetingDemandWaitRepository,
			meetingRepository,
			reportRepository,
			new MeetingDemandFactory(),
			new MeetingDemandPageNormalizer()
		);

		writer = UserFixture.createUser(WRITER_ID, "서버", 36);
		meetingDemand = createMeetingDemand(writer);
		setField(meetingDemand, "id", MEETING_DEMAND_ID);
	}

	@Nested
	class 모임_수요_생성 {

		@Test
		@DisplayName("요청한 키워드와 참여 정보를 가진 모임 수요를 생성한다.")
		void createMeetingDemand_success() {
			MeetingDemandV2CreateMeetingDemandBodyDto requestBody = createMeetingDemandRequest();
			given(userRepository.findByIdOrThrow(WRITER_ID)).willReturn(writer);
			given(meetingDemandRepository.save(any(MeetingDemand.class))).willAnswer(invocation -> {
				MeetingDemand savedMeetingDemand = invocation.getArgument(0);
				setField(savedMeetingDemand, "id", MEETING_DEMAND_ID);
				return savedMeetingDemand;
			});

			MeetingDemandV2CreateMeetingDemandResponseDto response = meetingDemandV2Service.createMeetingDemand(
				requestBody, WRITER_ID);

			ArgumentCaptor<MeetingDemand> captor = ArgumentCaptor.forClass(MeetingDemand.class);
			verify(meetingDemandRepository).save(captor.capture());

			MeetingDemand savedMeetingDemand = captor.getValue();
			assertThat(response.getMeetingDemandId()).isEqualTo(MEETING_DEMAND_ID);
			assertThat(savedMeetingDemand.getUserId()).isEqualTo(WRITER_ID);
			assertThat(savedMeetingDemand.getShortIntro()).isEqualTo("러닝 모임 열어주세요");
			assertThat(savedMeetingDemand.getExpectation()).isEqualTo("같이 꾸준히 달릴 수 있는 모임이 있으면 좋겠어요.");
			assertThat(savedMeetingDemand.getStatus()).isEqualTo(MeetingDemandStatus.BEFORE_OPEN);
			assertThat(savedMeetingDemand.getMeetingKeywordTypes())
				.containsExactly(MeetingKeywordType.EXERCISE, MeetingKeywordType.NETWORKING);
			assertThat(savedMeetingDemand.getJoinInfo()).isEqualTo(requestBody.getJoinInfo());
			assertThat(savedMeetingDemand.getWaitCount()).isZero();
			assertThat(savedMeetingDemand.getCommentCount()).isZero();
		}
	}

	@Nested
	class 모임_수요_삭제 {

		@Test
		@DisplayName("작성자가 개설 전 모임 수요를 삭제하면 기다려요 기록도 함께 삭제한다.")
		void deleteMeetingDemand_success() {
			given(meetingDemandRepository.findByIdWithPessimisticWriteLockOrThrow(MEETING_DEMAND_ID))
				.willReturn(meetingDemand);

			meetingDemandV2Service.deleteMeetingDemand(MEETING_DEMAND_ID, WRITER_ID);

			verify(meetingDemandWaitRepository).deleteAllByMeetingDemandId(MEETING_DEMAND_ID);
			verify(meetingDemandRepository).delete(meetingDemand);
		}

		@Test
		@DisplayName("개설 완료된 모임 수요는 삭제할 수 없다.")
		void deleteMeetingDemand_rejectsOpenedDemand() {
			meetingDemand.open();
			given(meetingDemandRepository.findByIdWithPessimisticWriteLockOrThrow(MEETING_DEMAND_ID))
				.willReturn(meetingDemand);

			assertThatThrownBy(() -> meetingDemandV2Service.deleteMeetingDemand(MEETING_DEMAND_ID, WRITER_ID))
				.isInstanceOf(BadRequestException.class);

			verify(meetingDemandWaitRepository, never()).deleteAllByMeetingDemandId(any());
			verify(meetingDemandRepository, never()).delete(any());
		}
	}

	@Nested
	class 모임_수요_기다려요_토글 {

		@Test
		@DisplayName("기다려요를 누르지 않은 유저는 기다려요를 추가한다.")
		void switchMeetingDemandWait_addsWait() {
			given(meetingDemandRepository.findByIdWithPessimisticWriteLockOrThrow(MEETING_DEMAND_ID))
				.willReturn(meetingDemand);
			given(meetingDemandWaitRepository.existsByMeetingDemandIdAndUserId(MEETING_DEMAND_ID, REQUEST_USER_ID))
				.willReturn(false);
			given(meetingDemandWaitRepository.countByMeetingDemandId(MEETING_DEMAND_ID)).willReturn(1L);

			MeetingDemandV2SwitchMeetingDemandWaitResponseDto response = meetingDemandV2Service.switchMeetingDemandWait(
				MEETING_DEMAND_ID, REQUEST_USER_ID);

			ArgumentCaptor<MeetingDemandWait> captor = ArgumentCaptor.forClass(MeetingDemandWait.class);
			verify(meetingDemandWaitRepository).save(captor.capture());
			verify(meetingDemandWaitRepository).flush();

			assertThat(captor.getValue().getMeetingDemandId()).isEqualTo(MEETING_DEMAND_ID);
			assertThat(captor.getValue().getUserId()).isEqualTo(REQUEST_USER_ID);
			assertThat(response.getIsWaiting()).isTrue();
			assertThat(response.getWaitCount()).isEqualTo(1);
			assertThat(meetingDemand.getWaitCount()).isEqualTo(1);
		}

		@Test
		@DisplayName("이미 기다려요를 누른 유저는 기다려요를 취소한다.")
		void switchMeetingDemandWait_removesWait() {
			meetingDemand.syncWaitCount(1);
			given(meetingDemandRepository.findByIdWithPessimisticWriteLockOrThrow(MEETING_DEMAND_ID))
				.willReturn(meetingDemand);
			given(meetingDemandWaitRepository.existsByMeetingDemandIdAndUserId(MEETING_DEMAND_ID, REQUEST_USER_ID))
				.willReturn(true);
			given(meetingDemandWaitRepository.countByMeetingDemandId(MEETING_DEMAND_ID)).willReturn(0L);

			MeetingDemandV2SwitchMeetingDemandWaitResponseDto response = meetingDemandV2Service.switchMeetingDemandWait(
				MEETING_DEMAND_ID, REQUEST_USER_ID);

			verify(meetingDemandWaitRepository).deleteByMeetingDemandIdAndUserId(MEETING_DEMAND_ID, REQUEST_USER_ID);
			verify(meetingDemandWaitRepository).flush();
			verify(meetingDemandWaitRepository, never()).save(any());

			assertThat(response.getIsWaiting()).isFalse();
			assertThat(response.getWaitCount()).isZero();
			assertThat(meetingDemand.getWaitCount()).isZero();
		}

		@Test
		@DisplayName("작성자는 자신의 모임 수요에 기다려요를 누를 수 없다.")
		void switchMeetingDemandWait_rejectsWriter() {
			given(meetingDemandRepository.findByIdWithPessimisticWriteLockOrThrow(MEETING_DEMAND_ID))
				.willReturn(meetingDemand);

			assertThatThrownBy(() -> meetingDemandV2Service.switchMeetingDemandWait(MEETING_DEMAND_ID, WRITER_ID))
				.isInstanceOf(BadRequestException.class);

			verify(meetingDemandWaitRepository, never()).save(any());
			verify(meetingDemandWaitRepository, never()).deleteByMeetingDemandIdAndUserId(any(), any());
		}
	}

	private MeetingDemand createMeetingDemand(User user) {
		return MeetingDemand.builder()
			.user(user)
			.shortIntro("러닝 모임 열어주세요")
			.expectation("같이 꾸준히 달릴 수 있는 모임이 있으면 좋겠어요.")
			.meetingKeywordTypes(List.of(MeetingKeywordType.EXERCISE, MeetingKeywordType.NETWORKING))
			.joinInfo(createJoinInfo())
			.build();
	}

	private MeetingDemandV2CreateMeetingDemandBodyDto createMeetingDemandRequest() {
		return new MeetingDemandV2CreateMeetingDemandBodyDto(
			"러닝 모임 열어주세요",
			"같이 꾸준히 달릴 수 있는 모임이 있으면 좋겠어요.",
			List.of("운동", "네트워킹"),
			createJoinInfo()
		);
	}

	private MeetingJoinInfo createJoinInfo() {
		return new MeetingJoinInfo(MeetingType.ONLINE, MeetingFrequency.STEADY);
	}
}
