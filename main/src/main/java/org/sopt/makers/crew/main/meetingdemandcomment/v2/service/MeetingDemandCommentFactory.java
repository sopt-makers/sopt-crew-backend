package org.sopt.makers.crew.main.meetingdemandcomment.v2.service;

import java.util.Optional;

import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemand;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandComment;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandCommentRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.request.MeetingDemandCommentV2CreateCommentBodyDto;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MeetingDemandCommentFactory {

	private static final int PARENT_COMMENT = 0;
	private static final int REPLY_COMMENT = 1;
	private static final int FIRST_REPLY_ORDER = 1;
	private static final Integer EMPTY_PARENT_ID = 0;

	private final MeetingDemandCommentRepository meetingDemandCommentRepository;

	public CreatedComment create(MeetingDemand meetingDemand, User writer,
		MeetingDemandCommentV2CreateCommentBodyDto requestBody) {
		int depth = PARENT_COMMENT;
		int order = 0;
		Integer parentId = EMPTY_PARENT_ID;
		MeetingDemandComment parentComment = null;

		if (!requestBody.getIsParent()) {
			parentComment = meetingDemandCommentRepository.findByIdAndMeetingDemandIdOrThrow(
				requestBody.getParentCommentId(), meetingDemand.getId());
			depth = REPLY_COMMENT;
			parentId = requestBody.getParentCommentId();
			order = getReplyOrder(parentId);
		}

		MeetingDemandComment comment = MeetingDemandComment.builder()
			.contents(requestBody.getContents())
			.depth(depth)
			.order(order)
			.user(writer)
			.meetingDemand(meetingDemand)
			.likeCount(0)
			.parentId(parentId)
			.build();

		return new CreatedComment(comment, parentComment);
	}

	private int getReplyOrder(Integer parentId) {
		Optional<MeetingDemandComment> recentComment = meetingDemandCommentRepository
			.findFirstByParentIdOrderByOrderDesc(parentId);
		return recentComment.map(comment -> comment.getOrder() + 1).orElse(FIRST_REPLY_ORDER);
	}

	public record CreatedComment(MeetingDemandComment comment, MeetingDemandComment parentComment) {
	}
}
