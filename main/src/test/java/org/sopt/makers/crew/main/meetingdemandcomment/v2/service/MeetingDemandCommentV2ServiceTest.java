package org.sopt.makers.crew.main.meetingdemandcomment.v2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingFrequency;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingType;
import org.sopt.makers.crew.main.entity.meeting.vo.MeetingJoinInfo;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemand;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandRepository;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandComment;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandCommentLike;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandCommentLikeRepository;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandCommentProfile;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandCommentRepository;
import org.sopt.makers.crew.main.entity.report.Report;
import org.sopt.makers.crew.main.entity.report.ReportRepository;
import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserFixture;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.ForbiddenException;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.meetingdemand.v2.service.MeetingDemandPageNormalizer;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.request.MeetingDemandCommentV2CreateCommentBodyDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandCommentV2CreateCommentResponseDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandCommentV2ReportCommentResponseDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandCommentV2SwitchCommentLikeResponseDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandCommentV2UpdateCommentResponseDto;

@ExtendWith(MockitoExtension.class)
class MeetingDemandCommentV2ServiceTest {

	private static final int MEETING_DEMAND_ID = 10;
	private static final int COMMENT_ID = 100;
	private static final int REPLY_COMMENT_ID = 101;
	private static final int RECENT_REPLY_ID = 102;
	private static final int WRITER_ID = 1;
	private static final int REQUEST_USER_ID = 2;
	private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 1, 12, 0);

	@Mock
	private MeetingDemandRepository meetingDemandRepository;

	@Mock
	private MeetingDemandCommentRepository meetingDemandCommentRepository;

	@Mock
	private MeetingDemandCommentLikeRepository meetingDemandCommentLikeRepository;

	@Mock
	private ReportRepository reportRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private MeetingDemandCommentProfileFactory meetingDemandCommentProfileFactory;

	@Mock
	private MeetingDemandCommentResponseFactory meetingDemandCommentResponseFactory;

	@Mock
	private MeetingDemandCommentNotificationSender meetingDemandCommentNotificationSender;

	@Mock
	private Time time;

	private MeetingDemandCommentV2ServiceImpl meetingDemandCommentV2Service;

	private User writer;
	private User requestUser;
	private MeetingDemand meetingDemand;
	private MeetingDemandComment parentComment;
	private MeetingDemandComment replyComment;
	private MeetingDemandCommentProfile writerProfile;

	@BeforeEach
	void setUp() {
		MeetingDemandCommentFactory meetingDemandCommentFactory = new MeetingDemandCommentFactory(
			meetingDemandCommentRepository);
		meetingDemandCommentV2Service = new MeetingDemandCommentV2ServiceImpl(
			meetingDemandRepository,
			meetingDemandCommentRepository,
			meetingDemandCommentLikeRepository,
			reportRepository,
			userRepository,
			new MeetingDemandPageNormalizer(),
			meetingDemandCommentFactory,
			meetingDemandCommentProfileFactory,
			meetingDemandCommentResponseFactory,
			meetingDemandCommentNotificationSender,
			time
		);

		writer = UserFixture.createUser(WRITER_ID, "서버", 36);
		requestUser = UserFixture.createUser(REQUEST_USER_ID, "기획", 36);
		meetingDemand = createMeetingDemand(writer);
		setField(meetingDemand, "id", MEETING_DEMAND_ID);

		parentComment = createComment(COMMENT_ID, "부모 댓글", 0, 0, writer, meetingDemand, COMMENT_ID);
		replyComment = createComment(REPLY_COMMENT_ID, "-~!@#@성실한 판다[1]%^&*+ 대댓글", 1, 1, writer,
			meetingDemand, COMMENT_ID);
		writerProfile = MeetingDemandCommentProfile.builder()
			.meetingDemandId(MEETING_DEMAND_ID)
			.userId(WRITER_ID)
			.build();
		setField(writerProfile, "anonymousNickname", "성실한 판다");
	}

	@Nested
	class 모임_수요_댓글_작성 {

		@Test
		@DisplayName("작성자가 자신의 수요에 부모 댓글을 작성하면 댓글 수만 증가시키고 알림을 보내지 않는다.")
		void createComment_createsParentComment() {
			MeetingDemandCommentV2CreateCommentBodyDto requestBody = new MeetingDemandCommentV2CreateCommentBodyDto(
				"부모 댓글", true, null);
			given(meetingDemandRepository.findByIdOrThrow(MEETING_DEMAND_ID)).willReturn(meetingDemand);
			given(userRepository.findByIdOrThrow(WRITER_ID)).willReturn(writer);
			given(meetingDemandCommentProfileFactory.findOrCreate(MEETING_DEMAND_ID, WRITER_ID))
				.willReturn(writerProfile);
			given(meetingDemandCommentRepository.save(any(MeetingDemandComment.class))).willAnswer(invocation -> {
				MeetingDemandComment savedComment = invocation.getArgument(0);
				setField(savedComment, "id", COMMENT_ID);
				return savedComment;
			});

			MeetingDemandCommentV2CreateCommentResponseDto response = meetingDemandCommentV2Service.createComment(
				MEETING_DEMAND_ID, requestBody, WRITER_ID);

			ArgumentCaptor<MeetingDemandComment> captor = ArgumentCaptor.forClass(MeetingDemandComment.class);
			verify(meetingDemandCommentRepository).save(captor.capture());
			verify(meetingDemandCommentNotificationSender, never()).sendCommentNotification(any(), any());

			MeetingDemandComment savedComment = captor.getValue();
			assertThat(response.getCommentId()).isEqualTo(COMMENT_ID);
			assertThat(savedComment.getContents()).isEqualTo("부모 댓글");
			assertThat(savedComment.getDepth()).isZero();
			assertThat(savedComment.getOrder()).isZero();
			assertThat(savedComment.getParentId()).isZero();
			assertThat(meetingDemand.getCommentCount()).isEqualTo(1);
		}

		@Test
		@DisplayName("대댓글을 작성하면 부모 댓글과 최근 order를 기준으로 다음 order를 계산한다.")
		void createComment_createsReplyComment() {
			MeetingDemandComment recentReply = createComment(RECENT_REPLY_ID, "최근 대댓글", 1, 2, requestUser,
				meetingDemand, COMMENT_ID);
			MeetingDemandCommentV2CreateCommentBodyDto requestBody = new MeetingDemandCommentV2CreateCommentBodyDto(
				"대댓글", false, COMMENT_ID);
			given(meetingDemandRepository.findByIdOrThrow(MEETING_DEMAND_ID)).willReturn(meetingDemand);
			given(userRepository.findByIdOrThrow(REQUEST_USER_ID)).willReturn(requestUser);
			given(meetingDemandCommentProfileFactory.findOrCreate(MEETING_DEMAND_ID, REQUEST_USER_ID))
				.willReturn(writerProfile);
			given(meetingDemandCommentRepository.findByIdAndMeetingDemandIdOrThrow(COMMENT_ID, MEETING_DEMAND_ID))
				.willReturn(parentComment);
			given(meetingDemandCommentRepository.findFirstByParentIdOrderByOrderDesc(COMMENT_ID))
				.willReturn(Optional.of(recentReply));
			given(meetingDemandCommentRepository.save(any(MeetingDemandComment.class))).willAnswer(invocation -> {
				MeetingDemandComment savedComment = invocation.getArgument(0);
				setField(savedComment, "id", REPLY_COMMENT_ID);
				return savedComment;
			});

			MeetingDemandCommentV2CreateCommentResponseDto response = meetingDemandCommentV2Service.createComment(
				MEETING_DEMAND_ID, requestBody, REQUEST_USER_ID);

			ArgumentCaptor<MeetingDemandComment> captor = ArgumentCaptor.forClass(MeetingDemandComment.class);
			verify(meetingDemandCommentRepository).save(captor.capture());
			verify(meetingDemandCommentNotificationSender).sendCommentNotification(meetingDemand, REQUEST_USER_ID);

			MeetingDemandComment savedComment = captor.getValue();
			assertThat(response.getCommentId()).isEqualTo(REPLY_COMMENT_ID);
			assertThat(savedComment.getContents()).isEqualTo("대댓글");
			assertThat(savedComment.getDepth()).isEqualTo(1);
			assertThat(savedComment.getOrder()).isEqualTo(3);
			assertThat(savedComment.getParentId()).isEqualTo(COMMENT_ID);
			assertThat(meetingDemand.getCommentCount()).isEqualTo(1);
		}
	}

	@Nested
	class 모임_수요_댓글_수정 {

		@Test
		@DisplayName("작성자는 댓글 내용을 수정할 수 있다.")
		void updateComment_success() {
			given(meetingDemandCommentRepository.findByIdOrThrow(COMMENT_ID)).willReturn(parentComment);
			given(time.now()).willReturn(NOW);

			MeetingDemandCommentV2UpdateCommentResponseDto response = meetingDemandCommentV2Service.updateComment(
				COMMENT_ID, "수정된 댓글", WRITER_ID);

			assertThat(response.getCommentId()).isEqualTo(COMMENT_ID);
			assertThat(response.getContents()).isEqualTo("수정된 댓글");
			assertThat(response.getUpdatedDate()).isEqualTo(String.valueOf(NOW));
			assertThat(parentComment.getContents()).isEqualTo("수정된 댓글");
		}

		@Test
		@DisplayName("작성자가 아니면 댓글을 수정할 수 없다.")
		void updateComment_rejectsNotWriter() {
			given(meetingDemandCommentRepository.findByIdOrThrow(COMMENT_ID)).willReturn(parentComment);

			assertThatThrownBy(() -> meetingDemandCommentV2Service.updateComment(COMMENT_ID, "수정", REQUEST_USER_ID))
				.isInstanceOf(ForbiddenException.class);
		}
	}

	@Nested
	class 모임_수요_댓글_삭제 {

		@Test
		@DisplayName("대댓글은 row를 삭제하고 좋아요 기록을 함께 삭제한다.")
		void deleteComment_deletesReplyComment() {
			meetingDemand.increaseCommentCount();
			given(meetingDemandCommentRepository.findByIdOrThrow(REPLY_COMMENT_ID)).willReturn(replyComment);
			given(meetingDemandRepository.findByIdOrThrow(MEETING_DEMAND_ID)).willReturn(meetingDemand);
			given(meetingDemandCommentRepository.findAllByParentIdAndDepthOrderByOrderDesc(REPLY_COMMENT_ID, 1))
				.willReturn(List.of());

			meetingDemandCommentV2Service.deleteComment(REPLY_COMMENT_ID, WRITER_ID);

			verify(meetingDemandCommentLikeRepository).deleteAllByMeetingDemandCommentId(REPLY_COMMENT_ID);
			verify(meetingDemandCommentRepository).delete(replyComment);
			assertThat(meetingDemand.getCommentCount()).isZero();
		}

		@Test
		@DisplayName("대댓글이 있는 부모 댓글은 삭제 표시 처리하고 자식 댓글의 멘션을 지운다.")
		void deleteComment_softDeletesParentCommentWhenReplyExists() {
			meetingDemand.increaseCommentCount();
			given(meetingDemandCommentRepository.findByIdOrThrow(COMMENT_ID)).willReturn(parentComment);
			given(meetingDemandRepository.findByIdOrThrow(MEETING_DEMAND_ID)).willReturn(meetingDemand);
			given(meetingDemandCommentRepository.findAllByParentIdAndDepthOrderByOrderDesc(COMMENT_ID, 1))
				.willReturn(List.of(replyComment));
			given(meetingDemandCommentProfileFactory.findOrCreate(MEETING_DEMAND_ID, WRITER_ID))
				.willReturn(writerProfile);

			meetingDemandCommentV2Service.deleteComment(COMMENT_ID, WRITER_ID);

			verify(meetingDemandCommentRepository, never()).delete(parentComment);
			verify(meetingDemandCommentLikeRepository, never()).deleteAllByMeetingDemandCommentId(COMMENT_ID);
			assertThat(parentComment.getContents()).isEqualTo("삭제된 댓글입니다.");
			assertThat(parentComment.getUser()).isNull();
			assertThat(parentComment.getUserId()).isNull();
			assertThat(replyComment.getContents()).isEqualTo("@_ 대댓글");
			assertThat(meetingDemand.getCommentCount()).isZero();
		}
	}

	@Nested
	class 모임_수요_댓글_좋아요_토글 {

		@Test
		@DisplayName("좋아요를 누르지 않은 댓글이면 좋아요를 추가한다.")
		void switchCommentLike_addsLike() {
			given(meetingDemandCommentRepository.findByIdOrThrow(COMMENT_ID)).willReturn(parentComment);
			given(meetingDemandCommentLikeRepository.existsByMeetingDemandCommentIdAndUserId(COMMENT_ID,
				REQUEST_USER_ID)).willReturn(false);

			MeetingDemandCommentV2SwitchCommentLikeResponseDto response = meetingDemandCommentV2Service
				.switchCommentLike(COMMENT_ID, REQUEST_USER_ID);

			ArgumentCaptor<MeetingDemandCommentLike> captor = ArgumentCaptor.forClass(MeetingDemandCommentLike.class);
			verify(meetingDemandCommentLikeRepository).save(captor.capture());
			assertThat(captor.getValue().getMeetingDemandCommentId()).isEqualTo(COMMENT_ID);
			assertThat(captor.getValue().getUserId()).isEqualTo(REQUEST_USER_ID);
			assertThat(response.getIsLiked()).isTrue();
			assertThat(parentComment.getLikeCount()).isEqualTo(1);
		}

		@Test
		@DisplayName("이미 좋아요를 누른 댓글이면 좋아요를 취소한다.")
		void switchCommentLike_removesLike() {
			parentComment.increaseLikeCount();
			given(meetingDemandCommentRepository.findByIdOrThrow(COMMENT_ID)).willReturn(parentComment);
			given(meetingDemandCommentLikeRepository.existsByMeetingDemandCommentIdAndUserId(COMMENT_ID,
				REQUEST_USER_ID)).willReturn(true);

			MeetingDemandCommentV2SwitchCommentLikeResponseDto response = meetingDemandCommentV2Service
				.switchCommentLike(COMMENT_ID, REQUEST_USER_ID);

			verify(meetingDemandCommentLikeRepository).deleteByMeetingDemandCommentIdAndUserId(COMMENT_ID,
				REQUEST_USER_ID);
			verify(meetingDemandCommentLikeRepository, never()).save(any());
			assertThat(response.getIsLiked()).isFalse();
			assertThat(parentComment.getLikeCount()).isZero();
		}
	}

	@Nested
	class 모임_수요_댓글_신고 {

		@Test
		@DisplayName("다른 사람이 작성한 모임 수요 댓글을 신고한다.")
		void reportComment_success() {
			Report report = Report.builder()
				.meetingDemandComment(parentComment)
				.meetingDemandCommentId(COMMENT_ID)
				.userId(REQUEST_USER_ID)
				.build();
			setField(report, "id", 40);
			given(meetingDemandCommentRepository.findByIdOrThrow(COMMENT_ID)).willReturn(parentComment);
			given(reportRepository.existsByMeetingDemandCommentIdAndUserId(COMMENT_ID, REQUEST_USER_ID))
				.willReturn(false);
			given(reportRepository.save(any(Report.class))).willReturn(report);

			MeetingDemandCommentV2ReportCommentResponseDto response = meetingDemandCommentV2Service.reportComment(
				COMMENT_ID, REQUEST_USER_ID);

			ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
			verify(reportRepository).save(captor.capture());
			assertThat(response.getReportId()).isEqualTo(40);
			assertThat(captor.getValue().getMeetingDemandComment()).isEqualTo(parentComment);
			assertThat(captor.getValue().getMeetingDemandCommentId()).isEqualTo(COMMENT_ID);
			assertThat(captor.getValue().getUserId()).isEqualTo(REQUEST_USER_ID);
		}

		@Test
		@DisplayName("작성자는 자신의 모임 수요 댓글을 신고할 수 없다.")
		void reportComment_rejectsWriter() {
			given(meetingDemandCommentRepository.findByIdOrThrow(COMMENT_ID)).willReturn(parentComment);

			assertThatThrownBy(() -> meetingDemandCommentV2Service.reportComment(COMMENT_ID, WRITER_ID))
				.isInstanceOf(ForbiddenException.class);

			verify(reportRepository, never()).save(any());
		}

		@Test
		@DisplayName("이미 신고한 모임 수요 댓글은 중복 신고할 수 없다.")
		void reportComment_rejectsDuplicatedReport() {
			given(meetingDemandCommentRepository.findByIdOrThrow(COMMENT_ID)).willReturn(parentComment);
			given(reportRepository.existsByMeetingDemandCommentIdAndUserId(COMMENT_ID, REQUEST_USER_ID))
				.willReturn(true);

			assertThatThrownBy(() -> meetingDemandCommentV2Service.reportComment(COMMENT_ID, REQUEST_USER_ID))
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
			.joinInfo(new MeetingJoinInfo(MeetingType.ONLINE, MeetingFrequency.STEADY))
			.build();
	}

	private MeetingDemandComment createComment(Integer id, String contents, int depth, int order, User user,
		MeetingDemand meetingDemand, Integer parentId) {
		MeetingDemandComment comment = MeetingDemandComment.builder()
			.contents(contents)
			.depth(depth)
			.order(order)
			.user(user)
			.meetingDemand(meetingDemand)
			.likeCount(0)
			.parentId(parentId)
			.build();
		setField(comment, "id", id);
		setField(comment, "userId", user.getId());
		setField(comment, "meetingDemandId", meetingDemand.getId());
		return comment;
	}
}
