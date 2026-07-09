package org.sopt.makers.crew.main.meetingdemandcomment.v2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingFrequency;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingType;
import org.sopt.makers.crew.main.entity.meeting.vo.MeetingJoinInfo;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemand;
import org.sopt.makers.crew.main.entity.meetingdemand.vo.MeetingDemandAnonymousProfile;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandComment;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandCommentLikeRepository;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandCommentProfile;
import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserFixture;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.external.playground.service.MemberBlockService;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandCommentDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandReplyDto;

@ExtendWith(MockitoExtension.class)
class MeetingDemandCommentResponseFactoryTest {

	private static final int MEETING_DEMAND_ID = 10;
	private static final int COMMENT_ID = 100;
	private static final int REPLY_COMMENT_ID = 101;
	private static final int WRITER_ID = 1;
	private static final int REQUEST_USER_ID = 2;
	private static final String ANONYMOUS_NICKNAME = "성실한 토마토";
	private static final int ANONYMOUS_IMAGE_NUMBER = 4;

	@Mock
	private MeetingDemandCommentLikeRepository meetingDemandCommentLikeRepository;

	@Mock
	private MeetingDemandCommentProfileFactory meetingDemandCommentProfileFactory;

	@Mock
	private UserRepository userRepository;

	@Mock
	private MemberBlockService memberBlockService;

	private MeetingDemandCommentResponseFactory meetingDemandCommentResponseFactory;

	private User writer;
	private User requestUser;
	private MeetingDemand meetingDemand;

	@BeforeEach
	void setUp() {
		meetingDemandCommentResponseFactory = new MeetingDemandCommentResponseFactory(
			meetingDemandCommentLikeRepository,
			meetingDemandCommentProfileFactory,
			userRepository,
			memberBlockService
		);

		writer = UserFixture.createUser(WRITER_ID, "서버", 36);
		requestUser = UserFixture.createUser(REQUEST_USER_ID, "기획", 36);
		meetingDemand = MeetingDemand.builder()
			.user(writer)
			.shortIntro("러닝 모임 열어주세요")
			.expectation("같이 꾸준히 달릴 수 있는 모임이 있으면 좋겠어요.")
			.meetingKeywordTypes(List.of(MeetingKeywordType.EXERCISE, MeetingKeywordType.NETWORKING))
			.joinInfo(new MeetingJoinInfo(MeetingType.ONLINE, MeetingFrequency.STEADY))
			.build();
		setField(meetingDemand, "id", MEETING_DEMAND_ID);
	}

	@Test
	@DisplayName("같은 작성자의 댓글과 대댓글은 같은 익명 프로필로 조회된다.")
	void createComments_usesSameAnonymousProfileForCommentAndReplyBySameWriter() {
		MeetingDemandComment parentComment = createComment(COMMENT_ID, "부모 댓글", 0, 0, writer, COMMENT_ID);
		MeetingDemandComment replyComment = createComment(REPLY_COMMENT_ID, "대댓글", 1, 1, writer, COMMENT_ID);
		MeetingDemandCommentProfile profile = MeetingDemandCommentProfile.builder()
			.meetingDemandId(MEETING_DEMAND_ID)
			.userId(WRITER_ID)
			.anonymousNickname(ANONYMOUS_NICKNAME)
			.anonymousImageNumber(ANONYMOUS_IMAGE_NUMBER)
			.build();
		List<MeetingDemandComment> visibleComments = List.of(parentComment, replyComment);
		given(meetingDemandCommentLikeRepository.findAllByMeetingDemandCommentIdInAndUserId(
			List.of(COMMENT_ID, REPLY_COMMENT_ID), REQUEST_USER_ID)).willReturn(List.of());
		given(meetingDemandCommentProfileFactory.createProfileMap(MEETING_DEMAND_ID, visibleComments))
			.willReturn(Map.of(WRITER_ID, profile));
		given(userRepository.findByIdOrThrow(REQUEST_USER_ID)).willReturn(requestUser);
		given(memberBlockService.getBlockedUsers((long)REQUEST_USER_ID, List.of((long)WRITER_ID)))
			.willReturn(Map.of((long)WRITER_ID, false));

		List<MeetingDemandCommentDto> comments = meetingDemandCommentResponseFactory.createComments(
			MEETING_DEMAND_ID, List.of(parentComment), List.of(replyComment), REQUEST_USER_ID);

		MeetingDemandCommentDto comment = comments.get(0);
		MeetingDemandReplyDto reply = comment.getReplies().get(0);
		assertThat(comment.getWriter().getAnonymousNickname()).isEqualTo(ANONYMOUS_NICKNAME);
		assertThat(reply.getWriter().getAnonymousNickname()).isEqualTo(comment.getWriter().getAnonymousNickname());
		assertThat(reply.getWriter().getAnonymousImageUrl()).isEqualTo(comment.getWriter().getAnonymousImageUrl());
		assertThat(comment.getWriter().getAnonymousImageUrl()).isEqualTo(
			MeetingDemandAnonymousProfile.getImageUrl(ANONYMOUS_IMAGE_NUMBER));
	}

	private MeetingDemandComment createComment(Integer id, String contents, int depth, int order, User user,
		Integer parentId) {
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
