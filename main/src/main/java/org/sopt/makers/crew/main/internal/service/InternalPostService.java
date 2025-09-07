package org.sopt.makers.crew.main.internal.service;

import static org.sopt.makers.crew.main.external.notification.PushNotificationEnums.*;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.Comparator;
import java.util.List;

import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.meeting.CoLeaderRepository;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.post.Post;
import org.sopt.makers.crew.main.entity.post.PostRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.external.notification.PushNotificationService;
import org.sopt.makers.crew.main.external.notification.dto.request.PushNotificationRequestDto;
import org.sopt.makers.crew.main.global.config.PushNotificationProperties;
import org.sopt.makers.crew.main.global.exception.ForbiddenException;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.sopt.makers.crew.main.global.exception.ServerException;
import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;
import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.global.util.MemberMentionConvertUtils;
import org.sopt.makers.crew.main.internal.dto.InternalPostCreateRequestDto;
import org.sopt.makers.crew.main.internal.dto.InternalPostCreateResponseDto;
import org.sopt.makers.crew.main.internal.dto.InternalPostGetAllResponseDto;
import org.sopt.makers.crew.main.internal.dto.InternalPostResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostDetailWithPartBaseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternalPostService {

	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final MeetingRepository meetingRepository;
	private final ApplyRepository applyRepository;
	private final CoLeaderRepository coLeaderRepository;
	private final PushNotificationService pushNotificationService;
	private final PushNotificationProperties pushNotificationProperties;

	public InternalPostGetAllResponseDto getPosts(PageOptionsDto pageOptionsDto, Integer orgId) {

		Pageable pageRequest = PageRequest.of(pageOptionsDto.getPage() - 1, pageOptionsDto.getTake());

		User user = userRepository.findById(orgId)
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_USER.getErrorCode()));

		Page<PostDetailWithPartBaseDto> postList =
			postRepository.findPostList(pageRequest, user.getId());

		List<InternalPostResponseDto> list = postList.getContent()
			.stream()
			.map(this::madeInternalResponseDto)
			.toList();

		PageMetaDto pageMetaDto = new PageMetaDto(pageOptionsDto, (int)postList.getTotalElements());

		return InternalPostGetAllResponseDto.of(list, pageMetaDto);
	}

	@Transactional
	public InternalPostCreateResponseDto createPost(InternalPostCreateRequestDto requestBody, Integer orgId) {
		Meeting meeting = meetingRepository.findByIdOrThrow(requestBody.meetingId());

		User user = userRepository.findById(orgId)
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_USER.getErrorCode()));

		List<Apply> applies = applyRepository.findAllByMeetingId(meeting.getId());

		boolean isInMeeting = applies.stream()
			.anyMatch(
				apply -> apply.getUserId().equals(user.getId()) && apply.getStatus().equals(EnApplyStatus.APPROVE));
		boolean isMeetingCreator = meeting.getUserId().equals(user.getId());
		boolean isCoLeader = coLeaderRepository.existsByMeetingIdAndUserId(meeting.getId(), user.getId());

		if (!isInMeeting && !isMeetingCreator && !isCoLeader) {
			throw new ForbiddenException(FORBIDDEN_EXCEPTION.getErrorCode());
		}

		Post post = Post.builder()
			.title(requestBody.title())
			.user(user)
			.contents(MemberMentionConvertUtils.convertMentionFormatToCrew(requestBody.contents()))
			.images(requestBody.images())
			.meeting(meeting)
			.build();

		Post savedPost = postRepository.save(post);

		List<String> userIdList = applyRepository.findAllByMeetingIdAndStatus(meeting.getId(), EnApplyStatus.APPROVE)
			.stream()
			.map(apply -> String.valueOf(apply.getUser().getId()))
			.toList();

		String[] userIds = userIdList.toArray(new String[0]);
		String pushNotificationContent = String.format("[%s의 새 글] : \"%s\"", user.getName(), post.getTitle());
		String pushNotificationWeblink = pushNotificationProperties.getPushWebUrl() + "/detail?id=" + meeting.getId();

		PushNotificationRequestDto pushRequestDto = PushNotificationRequestDto.of(userIds,
			NEW_POST_PUSH_NOTIFICATION_TITLE.getValue(), pushNotificationContent, PUSH_NOTIFICATION_CATEGORY.getValue(),
			pushNotificationWeblink);

		pushNotificationService.sendPushNotification(pushRequestDto);

		return InternalPostCreateResponseDto.from(savedPost.getId());
	}

	private InternalPostResponseDto madeInternalResponseDto(PostDetailWithPartBaseDto dto) {

		UserActivityVO recentActivity = dto.getUser().getPartInfo().stream()
			.filter(userActivityVO -> userActivityVO.getPart() != null)
			.max(Comparator.comparingInt(UserActivityVO::getGeneration))
			.orElseThrow(() -> new ServerException(INTERNAL_SERVER_ERROR.getErrorCode()));

		return InternalPostResponseDto.of(dto, recentActivity);
	}

}
