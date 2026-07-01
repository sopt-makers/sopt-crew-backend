package org.sopt.makers.crew.main.meetingdemand.v2.service;

import static org.sopt.makers.crew.main.external.notification.PushNotificationEnums.PUSH_NOTIFICATION_CATEGORY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemand;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandComment;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandCommentLike;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandCommentLikeRepository;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandCommentProfile;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandCommentProfileRepository;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandCommentRepository;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.external.notification.PushNotificationService;
import org.sopt.makers.crew.main.external.notification.dto.request.PushNotificationRequestDto;
import org.sopt.makers.crew.main.external.playground.service.MemberBlockService;
import org.sopt.makers.crew.main.global.config.PushNotificationProperties;
import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;
import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.global.util.MentionSecretStringRemover;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.query.MeetingDemandCommentV2GetCommentsQueryDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.request.MeetingDemandCommentV2CreateCommentBodyDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.request.MeetingDemandCommentV2MentionUserInCommentRequestDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandCommentDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandCommentV2CreateCommentResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandCommentV2GetCommentsResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandCommentV2SwitchCommentLikeResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandCommentV2UpdateCommentResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandReplyDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingDemandCommentV2ServiceImpl implements MeetingDemandCommentV2Service {

	private static final int PARENT_COMMENT = 0;
	private static final int REPLY_COMMENT = 1;
	private static final int FIRST_REPLY_ORDER = 1;
	private static final Integer EMPTY_PARENT_ID = 0;
	private static final String MEETING_DEMAND_COMMENT_TITLE = "나의 모임 수요에 새로운 댓글이 달렸어요";
	private static final String MEETING_DEMAND_REPLY_TITLE = "나의 모임 수요 댓글에 새로운 답글이 달렸어요";
	private static final String MEETING_DEMAND_MENTION_TITLE_FORMAT = "%s님이 회원님을 언급했어요";
	private static final String MEETING_DEMAND_WEB_LINK_FORMAT = "%s/meeting-demand?id=%d";

	private final MeetingDemandRepository meetingDemandRepository;
	private final MeetingDemandCommentRepository meetingDemandCommentRepository;
	private final MeetingDemandCommentProfileRepository meetingDemandCommentProfileRepository;
	private final MeetingDemandCommentLikeRepository meetingDemandCommentLikeRepository;
	private final UserRepository userRepository;
	private final MemberBlockService memberBlockService;
	private final PushNotificationService pushNotificationService;
	private final PushNotificationProperties pushNotificationProperties;
	private final Time time;

	@Override
	public MeetingDemandCommentV2GetCommentsResponseDto getComments(Integer meetingDemandId,
		MeetingDemandCommentV2GetCommentsQueryDto queryDto, Integer userId) {
		meetingDemandRepository.findByIdOrThrow(meetingDemandId);

		List<MeetingDemandComment> comments = meetingDemandCommentRepository
			.findAllByMeetingDemandIdOrderByCreatedTimestampAsc(meetingDemandId);
		List<Integer> commentIds = comments.stream()
			.map(MeetingDemandComment::getId)
			.toList();
		Set<Integer> likedCommentIds = getLikedCommentIds(commentIds, userId);
		Map<Integer, MeetingDemandCommentProfile> profileMap = getProfileMap(meetingDemandId, comments);
		Map<Long, Boolean> blockedUsers = getBlockedUsers(comments, userId);

		Map<Integer, List<MeetingDemandReplyDto>> replyMap = new HashMap<>();
		comments.stream()
			.filter(MeetingDemandComment::isReplyComment)
			.forEach(comment -> replyMap.computeIfAbsent(comment.getParentId(), key -> new ArrayList<>())
				.add(MeetingDemandReplyDto.of(comment, likedCommentIds.contains(comment.getId()),
					comment.isWriter(userId), isBlockedComment(comment, blockedUsers), profileMap)));

		List<MeetingDemandComment> parentComments = comments.stream()
			.filter(MeetingDemandComment::isParentComment)
			.toList();
		List<MeetingDemandComment> pagedParentComments = getPagedParentComments(parentComments, queryDto);
		List<MeetingDemandCommentDto> commentDtos = pagedParentComments.stream()
			.map(comment -> MeetingDemandCommentDto.of(comment, likedCommentIds.contains(comment.getId()),
				comment.isWriter(userId), replyMap.getOrDefault(comment.getId(), Collections.emptyList()),
				isBlockedComment(comment, blockedUsers), profileMap))
			.toList();

		PageMetaDto pageMetaDto = new PageMetaDto(queryDto, parentComments.size());

		return MeetingDemandCommentV2GetCommentsResponseDto.of(commentDtos, pageMetaDto);
	}

	@Override
	@Transactional
	public MeetingDemandCommentV2CreateCommentResponseDto createComment(Integer meetingDemandId,
		MeetingDemandCommentV2CreateCommentBodyDto requestBody, Integer userId) {
		MeetingDemand meetingDemand = meetingDemandRepository.findByIdOrThrow(meetingDemandId);
		User writer = userRepository.findByIdOrThrow(userId);
		MeetingDemandCommentProfile writerProfile = findOrCreateProfile(meetingDemandId, userId);

		int depth = PARENT_COMMENT;
		int order = 0;
		Integer parentId = EMPTY_PARENT_ID;
		MeetingDemandComment parentComment = null;

		if (!requestBody.getIsParent()) {
			parentComment = meetingDemandCommentRepository.findByIdAndMeetingDemandIdOrThrow(
				requestBody.getParentCommentId(), meetingDemandId);
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

		MeetingDemandComment savedComment = meetingDemandCommentRepository.save(comment);
		meetingDemand.increaseCommentCount();
		sendCommentPushNotification(requestBody, meetingDemand, parentComment, writerProfile);

		return MeetingDemandCommentV2CreateCommentResponseDto.of(savedComment.getId());
	}

	@Override
	@Transactional
	public MeetingDemandCommentV2UpdateCommentResponseDto updateComment(Integer commentId, String contents,
		Integer userId) {
		MeetingDemandComment comment = meetingDemandCommentRepository.findByIdOrThrow(commentId);

		comment.validateWriter(userId);
		comment.updateContents(contents);

		return MeetingDemandCommentV2UpdateCommentResponseDto.of(comment.getId(), comment.getContents(),
			String.valueOf(time.now()));
	}

	@Override
	@Transactional
	public void deleteComment(Integer commentId, Integer userId) {
		MeetingDemandComment comment = meetingDemandCommentRepository.findByIdOrThrow(commentId);
		comment.validateWriter(userId);

		MeetingDemand meetingDemand = meetingDemandRepository.findByIdOrThrow(comment.getMeetingDemandId());
		meetingDemand.decreaseCommentCount();

		List<MeetingDemandComment> childComments = meetingDemandCommentRepository
			.findAllByParentIdAndDepthOrderByOrderDesc(comment.getId(), REPLY_COMMENT);

		if (comment.isReplyComment() || childComments.isEmpty()) {
			meetingDemandCommentLikeRepository.deleteAllByMeetingDemandCommentId(commentId);
			meetingDemandCommentRepository.delete(comment);
			return;
		}

		MeetingDemandCommentProfile profile = findOrCreateProfile(comment.getMeetingDemandId(), userId);
		comment.deleteParentComment();
		deleteMentionsInChildComments(childComments, profile.getAnonymousNickname(), userId);
	}

	@Override
	@Transactional
	public MeetingDemandCommentV2SwitchCommentLikeResponseDto switchCommentLike(Integer commentId, Integer userId) {
		MeetingDemandComment comment = meetingDemandCommentRepository.findByIdOrThrow(commentId);

		boolean isLiked = meetingDemandCommentLikeRepository.existsByMeetingDemandCommentIdAndUserId(commentId, userId);
		if (isLiked) {
			meetingDemandCommentLikeRepository.deleteByMeetingDemandCommentIdAndUserId(commentId, userId);
			comment.decreaseLikeCount();
			return MeetingDemandCommentV2SwitchCommentLikeResponseDto.of(false);
		}

		MeetingDemandCommentLike like = MeetingDemandCommentLike.builder()
			.meetingDemandCommentId(commentId)
			.userId(userId)
			.build();

		meetingDemandCommentLikeRepository.save(like);
		comment.increaseLikeCount();

		return MeetingDemandCommentV2SwitchCommentLikeResponseDto.of(true);
	}

	@Override
	@Transactional
	public void mentionUserInComment(MeetingDemandCommentV2MentionUserInCommentRequestDto requestBody, Integer userId) {
		meetingDemandRepository.findByIdOrThrow(requestBody.getMeetingDemandId());
		userRepository.findByIdOrThrow(userId);

		MeetingDemandCommentProfile writerProfile = findOrCreateProfile(requestBody.getMeetingDemandId(), userId);
		String title = String.format(MEETING_DEMAND_MENTION_TITLE_FORMAT, writerProfile.getAnonymousNickname());
		String content = "\"" + MentionSecretStringRemover.removeSecretString(requestBody.getContent()) + "\"";
		String webLink = createMeetingDemandWebLink(requestBody.getMeetingDemandId());

		String[] userOrgIds = requestBody.getOrgIds().stream()
			.map(Object::toString)
			.toArray(String[]::new);

		PushNotificationRequestDto pushRequestDto = PushNotificationRequestDto.of(
			userOrgIds,
			title,
			content,
			PUSH_NOTIFICATION_CATEGORY.getValue(),
			webLink
		);

		pushNotificationService.sendPushNotification(pushRequestDto);
	}

	private MeetingDemandCommentProfile findOrCreateProfile(Integer meetingDemandId, Integer userId) {
		return meetingDemandCommentProfileRepository.findByMeetingDemandIdAndUserId(meetingDemandId, userId)
			.orElseGet(() -> meetingDemandCommentProfileRepository.save(MeetingDemandCommentProfile.builder()
				.meetingDemandId(meetingDemandId)
				.userId(userId)
				.build()));
	}

	private int getReplyOrder(Integer parentId) {
		Optional<MeetingDemandComment> recentComment = meetingDemandCommentRepository
			.findFirstByParentIdOrderByOrderDesc(parentId);
		return recentComment.map(comment -> comment.getOrder() + 1).orElse(FIRST_REPLY_ORDER);
	}

	private Set<Integer> getLikedCommentIds(List<Integer> commentIds, Integer userId) {
		if (commentIds.isEmpty()) {
			return Set.of();
		}

		return meetingDemandCommentLikeRepository.findAllByMeetingDemandCommentIdInAndUserId(commentIds, userId)
			.stream()
			.map(MeetingDemandCommentLike::getMeetingDemandCommentId)
			.collect(Collectors.toSet());
	}

	private Map<Integer, MeetingDemandCommentProfile> getProfileMap(Integer meetingDemandId,
		List<MeetingDemandComment> comments) {
		Set<Integer> userIds = comments.stream()
			.map(MeetingDemandComment::getUserId)
			.filter(userId -> userId != null)
			.collect(Collectors.toSet());
		if (userIds.isEmpty()) {
			return Map.of();
		}

		return meetingDemandCommentProfileRepository.findAllByMeetingDemandIdAndUserIdIn(meetingDemandId, userIds)
			.stream()
			.collect(Collectors.toMap(MeetingDemandCommentProfile::getUserId, Function.identity()));
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

	private List<MeetingDemandComment> getPagedParentComments(List<MeetingDemandComment> parentComments,
		PageOptionsDto pageOptionsDto) {
		int fromIndex = Math.min((pageOptionsDto.getPage() - 1) * pageOptionsDto.getTake(), parentComments.size());
		int toIndex = Math.min(fromIndex + pageOptionsDto.getTake(), parentComments.size());

		return parentComments.subList(fromIndex, toIndex);
	}

	private void sendCommentPushNotification(MeetingDemandCommentV2CreateCommentBodyDto requestBody,
		MeetingDemand meetingDemand, MeetingDemandComment parentComment, MeetingDemandCommentProfile writerProfile) {
		boolean isReplyComment = !requestBody.getIsParent();
		Integer receiverUserId = isReplyComment ? getReplyReceiverUserId(parentComment) : meetingDemand.getUserId();
		if (receiverUserId == null) {
			return;
		}

		String title = isReplyComment ? MEETING_DEMAND_REPLY_TITLE : MEETING_DEMAND_COMMENT_TITLE;
		String commentType = isReplyComment ? "답글" : "댓글";
		String secretStringRemovedContent = MentionSecretStringRemover.removeSecretString(requestBody.getContents());
		String pushNotificationContent = String.format("[%s의 %s] : \"%s\"",
			writerProfile.getAnonymousNickname(), commentType, secretStringRemovedContent);
		String webLink = createMeetingDemandWebLink(meetingDemand.getId());

		PushNotificationRequestDto pushRequestDto = PushNotificationRequestDto.of(
			new String[] {String.valueOf(receiverUserId)},
			title,
			pushNotificationContent,
			PUSH_NOTIFICATION_CATEGORY.getValue(),
			webLink
		);

		pushNotificationService.sendPushNotification(pushRequestDto);
	}

	private Integer getReplyReceiverUserId(MeetingDemandComment parentComment) {
		if (parentComment == null) {
			return null;
		}

		return parentComment.getUserId();
	}

	private String createMeetingDemandWebLink(Integer meetingDemandId) {
		return String.format(MEETING_DEMAND_WEB_LINK_FORMAT, pushNotificationProperties.getPushWebUrl(),
			meetingDemandId);
	}

	private void deleteMentionsInChildComments(List<MeetingDemandComment> childComments, String mentionName,
		Integer mentionUserId) {
		childComments.forEach(comment -> {
			String deletedMentionContent = MentionSecretStringRemover.deleteMentionContent(comment.getContents(),
				mentionName, mentionUserId.toString());
			comment.updateContents(deletedMentionContent);
		});
	}
}
