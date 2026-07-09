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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingFrequency;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingType;
import org.sopt.makers.crew.main.entity.meeting.vo.MeetingJoinInfo;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemand;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandRepository;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandWait;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandWaitHistory;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandWaitHistoryRepository;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandWaitRepository;
import org.sopt.makers.crew.main.entity.meetingdemand.enums.MeetingDemandStatus;
import org.sopt.makers.crew.main.entity.report.Report;
import org.sopt.makers.crew.main.entity.report.ReportRepository;
import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserFixture;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.ForbiddenException;
import org.sopt.makers.crew.main.global.exception.UnAuthorizedException;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.request.MeetingDemandV2CreateMeetingDemandBodyDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.query.MeetingDemandV2GetMeetingDemandsQueryDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2GetMeetingDemandsResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2CreateMeetingDemandResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2ReportResponseDto;
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
	private MeetingDemandWaitHistoryRepository meetingDemandWaitHistoryRepository;

	@Mock
	private MeetingRepository meetingRepository;

	@Mock
	private ReportRepository reportRepository;

	@Mock
	private MeetingDemandNotificationSender meetingDemandNotificationSender;

	private MeetingDemandV2ServiceImpl meetingDemandV2Service;

	private User writer;
	private MeetingDemand meetingDemand;

	@BeforeEach
	void setUp() {
		meetingDemandV2Service = new MeetingDemandV2ServiceImpl(
			userRepository,
			meetingDemandRepository,
			meetingDemandWaitRepository,
			meetingDemandWaitHistoryRepository,
			meetingRepository,
			reportRepository,
			new MeetingDemandFactory(),
			new MeetingDemandPageNormalizer(),
			meetingDemandNotificationSender
		);

		writer = UserFixture.createUser(WRITER_ID, "서버", 36);
		meetingDemand = createMeetingDemand(writer);
		setField(meetingDemand, "id", MEETING_DEMAND_ID);
	}

	@Nested
	class 모임_수요_목록_조회 {

		@Test
		@DisplayName("개설 전과 개설 완료 상태를 모두 최신순 목록으로 조회한다.")
		void getMeetingDemands_returnsAllStatuses() {
			User requestUser = UserFixture.createUser(REQUEST_USER_ID, "서버", 36);
			MeetingDemand ownMeetingDemand = createMeetingDemand(requestUser);
			setField(ownMeetingDemand, "id", MEETING_DEMAND_ID + 1);
			MeetingDemand openedMeetingDemand = createMeetingDemand(writer);
			setField(openedMeetingDemand, "id", MEETING_DEMAND_ID + 2);
			openedMeetingDemand.open();
			MeetingDemandWait wait = MeetingDemandWait.builder()
				.meetingDemandId(openedMeetingDemand.getId())
				.userId(REQUEST_USER_ID)
				.build();
			given(meetingDemandRepository.count()).willReturn(3L);
			given(meetingDemandRepository.findAll(any(Pageable.class)))
				.willReturn(new PageImpl<>(List.of(meetingDemand, ownMeetingDemand, openedMeetingDemand)));
			given(meetingDemandWaitRepository.findAllByMeetingDemandIdInAndUserId(
				List.of(meetingDemand.getId(), ownMeetingDemand.getId(), openedMeetingDemand.getId()), REQUEST_USER_ID))
				.willReturn(List.of(wait));

			MeetingDemandV2GetMeetingDemandsResponseDto response = meetingDemandV2Service.getMeetingDemands(
				new MeetingDemandV2GetMeetingDemandsQueryDto(1, 3), REQUEST_USER_ID);

			assertThat(response.meetingDemands()).hasSize(3);
			assertThat(response.meetingDemands().get(0).id()).isEqualTo(MEETING_DEMAND_ID);
			assertThat(response.meetingDemands().get(0).status()).isEqualTo(MeetingDemandStatus.BEFORE_OPEN.name());
			assertThat(response.meetingDemands().get(0).isMine()).isFalse();
			assertThat(response.meetingDemands().get(0).isWaiting()).isFalse();
			assertThat(response.meetingDemands().get(1).id()).isEqualTo(MEETING_DEMAND_ID + 1);
			assertThat(response.meetingDemands().get(1).status()).isEqualTo(MeetingDemandStatus.BEFORE_OPEN.name());
			assertThat(response.meetingDemands().get(1).isMine()).isTrue();
			assertThat(response.meetingDemands().get(1).isWaiting()).isFalse();
			assertThat(response.meetingDemands().get(2).id()).isEqualTo(MEETING_DEMAND_ID + 2);
			assertThat(response.meetingDemands().get(2).status()).isEqualTo(MeetingDemandStatus.OPENED.name());
			assertThat(response.meetingDemands().get(2).isMine()).isFalse();
			assertThat(response.meetingDemands().get(2).isWaiting()).isTrue();
			assertThat(response.meta().getItemCount()).isEqualTo(3);
			verify(meetingDemandRepository, never()).countByStatus(any(MeetingDemandStatus.class));
			verify(meetingDemandRepository, never()).findAllByStatus(any(MeetingDemandStatus.class), any());
		}
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

		@Test
		@DisplayName("참여 정보 없이 모임 수요를 생성한다.")
		void createMeetingDemand_withoutJoinInfo_success() {
			MeetingDemandV2CreateMeetingDemandBodyDto requestBody = new MeetingDemandV2CreateMeetingDemandBodyDto(
				"러닝 모임 열어주세요",
				"같이 꾸준히 달릴 수 있는 모임이 있으면 좋겠어요.",
				List.of("운동", "네트워킹"),
				null
			);
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

			assertThat(response.getMeetingDemandId()).isEqualTo(MEETING_DEMAND_ID);
			assertThat(captor.getValue().getJoinInfo()).isNull();
		}

		@Test
		@DisplayName("빈 참여 정보 객체는 저장하지 않는다.")
		void createMeetingDemand_convertsEmptyJoinInfoToNull() {
			MeetingDemandV2CreateMeetingDemandBodyDto requestBody = new MeetingDemandV2CreateMeetingDemandBodyDto(
				"러닝 모임 열어주세요",
				"같이 꾸준히 달릴 수 있는 모임이 있으면 좋겠어요.",
				List.of("운동", "네트워킹"),
				new MeetingJoinInfo(null, null)
			);
			given(userRepository.findByIdOrThrow(WRITER_ID)).willReturn(writer);
			given(meetingDemandRepository.save(any(MeetingDemand.class))).willAnswer(invocation -> {
				MeetingDemand savedMeetingDemand = invocation.getArgument(0);
				setField(savedMeetingDemand, "id", MEETING_DEMAND_ID);
				return savedMeetingDemand;
			});

			meetingDemandV2Service.createMeetingDemand(requestBody, WRITER_ID);

			ArgumentCaptor<MeetingDemand> captor = ArgumentCaptor.forClass(MeetingDemand.class);
			verify(meetingDemandRepository).save(captor.capture());
			assertThat(captor.getValue().getJoinInfo()).isNull();
		}

		@Test
		@DisplayName("참여 방식 또는 참여 강도만 선택해도 모임 수요를 생성한다.")
		void createMeetingDemand_withPartialJoinInfo_success() {
			MeetingJoinInfo joinInfo = new MeetingJoinInfo(MeetingType.ONLINE, null);
			MeetingDemandV2CreateMeetingDemandBodyDto requestBody = new MeetingDemandV2CreateMeetingDemandBodyDto(
				"러닝 모임 열어주세요",
				"같이 꾸준히 달릴 수 있는 모임이 있으면 좋겠어요.",
				List.of("운동", "네트워킹"),
				joinInfo
			);
			given(userRepository.findByIdOrThrow(WRITER_ID)).willReturn(writer);
			given(meetingDemandRepository.save(any(MeetingDemand.class))).willAnswer(invocation -> {
				MeetingDemand savedMeetingDemand = invocation.getArgument(0);
				setField(savedMeetingDemand, "id", MEETING_DEMAND_ID);
				return savedMeetingDemand;
			});

			meetingDemandV2Service.createMeetingDemand(requestBody, WRITER_ID);

			ArgumentCaptor<MeetingDemand> captor = ArgumentCaptor.forClass(MeetingDemand.class);
			verify(meetingDemandRepository).save(captor.capture());
			assertThat(captor.getValue().getJoinInfo()).isEqualTo(joinInfo);
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
			given(meetingDemandWaitHistoryRepository.existsByMeetingDemandIdAndUserId(MEETING_DEMAND_ID,
				REQUEST_USER_ID)).willReturn(false);
			given(meetingDemandWaitRepository.countByMeetingDemandId(MEETING_DEMAND_ID)).willReturn(1L);

			MeetingDemandV2SwitchMeetingDemandWaitResponseDto response = meetingDemandV2Service.switchMeetingDemandWait(
				MEETING_DEMAND_ID, REQUEST_USER_ID);

			ArgumentCaptor<MeetingDemandWait> captor = ArgumentCaptor.forClass(MeetingDemandWait.class);
			verify(userRepository).findByIdOrThrow(REQUEST_USER_ID);
			verify(meetingDemandWaitRepository).save(captor.capture());
			verify(meetingDemandWaitHistoryRepository).save(any(MeetingDemandWaitHistory.class));
			verify(meetingDemandNotificationSender).sendWaitNotification(meetingDemand);
			verify(meetingDemandWaitRepository).flush();

			assertThat(captor.getValue().getMeetingDemandId()).isEqualTo(MEETING_DEMAND_ID);
			assertThat(captor.getValue().getUserId()).isEqualTo(REQUEST_USER_ID);
			assertThat(response.getIsWaiting()).isTrue();
			assertThat(response.getWaitCount()).isEqualTo(1);
			assertThat(meetingDemand.getWaitCount()).isEqualTo(1);
		}

		@Test
		@DisplayName("기다려요를 다시 누른 이력이 있는 유저에게는 알림을 반복 발송하지 않는다.")
		void switchMeetingDemandWait_doesNotSendDuplicatedWaitNotification() {
			given(meetingDemandRepository.findByIdWithPessimisticWriteLockOrThrow(MEETING_DEMAND_ID))
				.willReturn(meetingDemand);
			given(meetingDemandWaitRepository.existsByMeetingDemandIdAndUserId(MEETING_DEMAND_ID, REQUEST_USER_ID))
				.willReturn(false);
			given(meetingDemandWaitHistoryRepository.existsByMeetingDemandIdAndUserId(MEETING_DEMAND_ID,
				REQUEST_USER_ID)).willReturn(true);
			given(meetingDemandWaitRepository.countByMeetingDemandId(MEETING_DEMAND_ID)).willReturn(1L);

			MeetingDemandV2SwitchMeetingDemandWaitResponseDto response = meetingDemandV2Service.switchMeetingDemandWait(
				MEETING_DEMAND_ID, REQUEST_USER_ID);

			verify(userRepository).findByIdOrThrow(REQUEST_USER_ID);
			verify(meetingDemandWaitRepository).save(any(MeetingDemandWait.class));
			verify(meetingDemandWaitHistoryRepository, never()).save(any());
			verify(meetingDemandNotificationSender, never()).sendWaitNotification(any());
			assertThat(response.getIsWaiting()).isTrue();
			assertThat(response.getWaitCount()).isEqualTo(1);
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

			verify(userRepository).findByIdOrThrow(REQUEST_USER_ID);
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

		@Test
		@DisplayName("인증된 유저 정보를 찾을 수 없으면 기다려요를 저장하지 않는다.")
		void switchMeetingDemandWait_rejectsUnknownUser() {
			given(meetingDemandRepository.findByIdWithPessimisticWriteLockOrThrow(MEETING_DEMAND_ID))
				.willReturn(meetingDemand);
			given(userRepository.findByIdOrThrow(REQUEST_USER_ID)).willThrow(new UnAuthorizedException());

			assertThatThrownBy(() -> meetingDemandV2Service.switchMeetingDemandWait(MEETING_DEMAND_ID,
				REQUEST_USER_ID))
				.isInstanceOf(UnAuthorizedException.class);

			verify(meetingDemandWaitRepository, never()).existsByMeetingDemandIdAndUserId(any(), any());
			verify(meetingDemandWaitRepository, never()).save(any());
			verify(meetingDemandWaitHistoryRepository, never()).save(any());
		}
	}

	@Nested
	class 모임_수요_신고 {

		@Test
		@DisplayName("다른 사람이 작성한 모임 수요를 신고한다.")
		void reportMeetingDemand_success() {
			Report report = Report.builder()
				.meetingDemand(meetingDemand)
				.meetingDemandId(MEETING_DEMAND_ID)
				.userId(REQUEST_USER_ID)
				.build();
			setField(report, "id", 30);
			given(meetingDemandRepository.findByIdOrThrow(MEETING_DEMAND_ID)).willReturn(meetingDemand);
			given(reportRepository.existsByMeetingDemandIdAndUserId(MEETING_DEMAND_ID, REQUEST_USER_ID))
				.willReturn(false);
			given(reportRepository.save(any(Report.class))).willReturn(report);

			MeetingDemandV2ReportResponseDto response = meetingDemandV2Service.reportMeetingDemand(MEETING_DEMAND_ID,
				REQUEST_USER_ID);

			ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
			verify(reportRepository).save(captor.capture());
			assertThat(response.getReportId()).isEqualTo(30);
			assertThat(captor.getValue().getMeetingDemand()).isEqualTo(meetingDemand);
			assertThat(captor.getValue().getMeetingDemandId()).isEqualTo(MEETING_DEMAND_ID);
			assertThat(captor.getValue().getUserId()).isEqualTo(REQUEST_USER_ID);
		}

		@Test
		@DisplayName("작성자는 자신의 모임 수요를 신고할 수 없다.")
		void reportMeetingDemand_rejectsWriter() {
			given(meetingDemandRepository.findByIdOrThrow(MEETING_DEMAND_ID)).willReturn(meetingDemand);

			assertThatThrownBy(() -> meetingDemandV2Service.reportMeetingDemand(MEETING_DEMAND_ID, WRITER_ID))
				.isInstanceOf(ForbiddenException.class);

			verify(reportRepository, never()).save(any());
		}

		@Test
		@DisplayName("이미 신고한 모임 수요는 중복 신고할 수 없다.")
		void reportMeetingDemand_rejectsDuplicatedReport() {
			given(meetingDemandRepository.findByIdOrThrow(MEETING_DEMAND_ID)).willReturn(meetingDemand);
			given(reportRepository.existsByMeetingDemandIdAndUserId(MEETING_DEMAND_ID, REQUEST_USER_ID))
				.willReturn(true);

			assertThatThrownBy(() -> meetingDemandV2Service.reportMeetingDemand(MEETING_DEMAND_ID, REQUEST_USER_ID))
				.isInstanceOf(BadRequestException.class);

			verify(reportRepository, never()).save(any());
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
