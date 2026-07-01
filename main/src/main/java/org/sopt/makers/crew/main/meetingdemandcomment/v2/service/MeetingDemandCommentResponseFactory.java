package org.sopt.makers.crew.main.meetingdemandcomment.v2.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandComment;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandCommentLike;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandCommentLikeRepository;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandCommentProfile;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.external.playground.service.MemberBlockService;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandCommentDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandReplyDto;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MeetingDemandCommentResponseFactory {

	private final MeetingDemandCommentLikeRepository meetingDemandCommentLikeRepository;
	private final MeetingDemandCommentProfileFactory meetingDemandCommentProfileFactory;
	private final UserRepository userRepository;
	private final MemberBlockService memberBlockService;

	public List<MeetingDemandCommentDto> createComments(Integer meetingDemandId,
		List<MeetingDemandComment> parentComments, List<MeetingDemandComment> replyComments, Integer userId) {
		List<MeetingDemandComment> visibleComments = new ArrayList<>();
		visibleComments.addAll(parentComments);
		visibleComments.addAll(replyComments);

		Set<Integer> likedCommentIds = getLikedCommentIds(visibleComments, userId);
		Map<Integer, MeetingDemandCommentProfile> profileMap = meetingDemandCommentProfileFactory.createProfileMap(
			meetingDemandId, visibleComments);
		Map<Long, Boolean> blockedUsers = getBlockedUsers(visibleComments, userId);
		Map<Integer, List<MeetingDemandReplyDto>> replyMap = createReplyMap(replyComments, likedCommentIds, userId,
			blockedUsers, profileMap);

		return parentComments.stream()
			.map(comment -> MeetingDemandCommentDto.of(comment, likedCommentIds.contains(comment.getId()),
				comment.isWriter(userId), replyMap.getOrDefault(comment.getId(), Collections.emptyList()),
				isBlockedComment(comment, blockedUsers), profileMap))
			.toList();
	}

	private Map<Integer, List<MeetingDemandReplyDto>> createReplyMap(List<MeetingDemandComment> replyComments,
		Set<Integer> likedCommentIds, Integer userId, Map<Long, Boolean> blockedUsers,
		Map<Integer, MeetingDemandCommentProfile> profileMap) {
		Map<Integer, List<MeetingDemandReplyDto>> replyMap = new HashMap<>();
		replyComments.forEach(comment -> replyMap.computeIfAbsent(comment.getParentId(), key -> new ArrayList<>())
			.add(MeetingDemandReplyDto.of(comment, likedCommentIds.contains(comment.getId()),
				comment.isWriter(userId), isBlockedComment(comment, blockedUsers), profileMap)));

		return replyMap;
	}

	private Set<Integer> getLikedCommentIds(List<MeetingDemandComment> comments, Integer userId) {
		List<Integer> commentIds = comments.stream()
			.map(MeetingDemandComment::getId)
			.toList();
		if (commentIds.isEmpty()) {
			return Set.of();
		}

		return meetingDemandCommentLikeRepository.findAllByMeetingDemandCommentIdInAndUserId(commentIds, userId)
			.stream()
			.map(MeetingDemandCommentLike::getMeetingDemandCommentId)
			.collect(Collectors.toSet());
	}

	private Map<Long, Boolean> getBlockedUsers(List<MeetingDemandComment> comments, Integer userId) {
		List<Long> userOrgIds = comments.stream()
			.map(MeetingDemandComment::getUserId)
			.filter(commentUserId -> commentUserId != null)
			.map(Integer::longValue)
			.distinct()
			.toList();
		if (userOrgIds.isEmpty()) {
			return Map.of();
		}

		User user = userRepository.findByIdOrThrow(userId);
		return memberBlockService.getBlockedUsers(user.getId().longValue(), userOrgIds);
	}

	private boolean isBlockedComment(MeetingDemandComment comment, Map<Long, Boolean> blockedUsers) {
		if (comment.getUserId() == null) {
			return false;
		}

		return blockedUsers.getOrDefault(comment.getUserId().longValue(), false);
	}
}
