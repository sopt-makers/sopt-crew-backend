package org.sopt.makers.crew.main.post.v2.service;

import static java.util.stream.Collectors.toList;
import static org.sopt.makers.crew.main.common.exception.ErrorStatus.ALREADY_REPORTED_POST;
import static org.sopt.makers.crew.main.common.exception.ErrorStatus.FORBIDDEN_EXCEPTION;
import static org.sopt.makers.crew.main.common.exception.ErrorStatus.MAX_IMAGE_UPLOAD_EXCEEDED;
import static org.sopt.makers.crew.main.internal.notification.PushNotificationEnums.NEW_POST_MENTION_PUSH_NOTIFICATION_TITLE;
import static org.sopt.makers.crew.main.internal.notification.PushNotificationEnums.NEW_POST_PUSH_NOTIFICATION_TITLE;
import static org.sopt.makers.crew.main.internal.notification.PushNotificationEnums.PUSH_NOTIFICATION_CATEGORY;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.sopt.makers.crew.main.common.exception.BadRequestException;
import org.sopt.makers.crew.main.common.exception.ForbiddenException;
import org.sopt.makers.crew.main.common.pagination.dto.PageMetaDto;
import org.sopt.makers.crew.main.common.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.common.util.CustomPageable;
import org.sopt.makers.crew.main.common.util.Time;
import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.comment.Comment;
import org.sopt.makers.crew.main.entity.comment.CommentRepository;
import org.sopt.makers.crew.main.entity.like.Like;
import org.sopt.makers.crew.main.entity.like.LikeRepository;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.post.Post;
import org.sopt.makers.crew.main.entity.post.PostRepository;
import org.sopt.makers.crew.main.entity.report.Report;
import org.sopt.makers.crew.main.entity.report.ReportRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.internal.notification.PushNotificationService;
import org.sopt.makers.crew.main.internal.notification.dto.PushNotificationRequestDto;
import org.sopt.makers.crew.main.post.v2.dto.query.PostGetPostsCommand;
import org.sopt.makers.crew.main.post.v2.dto.request.PostV2CreatePostBodyDto;
import org.sopt.makers.crew.main.post.v2.dto.request.PostV2MentionUserInPostRequestDto;
import org.sopt.makers.crew.main.post.v2.dto.request.PostV2UpdatePostBodyDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostDetailBaseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostDetailResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2CreatePostResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2GetPostCountResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2GetPostsResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2ReportResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2SwitchPostLikeResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2UpdatePostResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostV2ServiceImpl implements PostV2Service {

	private final MeetingRepository meetingRepository;
	private final UserRepository userRepository;
	private final PostRepository postRepository;
	private final ApplyRepository applyRepository;
	private final CommentRepository commentRepository;
	private final LikeRepository likeRepository;
	private final ReportRepository reportRepository;

	private final PushNotificationService pushNotificationService;

	@Value("${push-notification.web-url}")
	private String pushWebUrl;

	private final Time time;

	/**
	 * 모임 게시글 작성
	 *
	 * @throws 403 모임에 속한 유저가 아닌 경우
	 * @apiNote 모임에 속한 유저만 작성 가능
	 */
	@Override
	@Transactional
	public PostV2CreatePostResponseDto createPost(PostV2CreatePostBodyDto requestBody,
		Integer userId) {
		Meeting meeting = meetingRepository.findByIdOrThrow(requestBody.getMeetingId());
		User user = userRepository.findByIdOrThrow(userId);

		List<Apply> applies = applyRepository.findAllByMeetingId(meeting.getId());

		boolean isInMeeting = applies.stream()
			.anyMatch(apply -> apply.getUserId().equals(userId)
				&& apply.getStatus().equals(EnApplyStatus.APPROVE));

		boolean isMeetingCreator = meeting.getUserId().equals(userId);

		if (isInMeeting == false && isMeetingCreator == false) {
			throw new ForbiddenException(FORBIDDEN_EXCEPTION.getErrorCode());
		}

		Post post = Post.builder()
			.title(requestBody.getTitle())
			.user(user)
			.contents(requestBody.getContents())
			.images(requestBody.getImages())
			.meeting(meeting)
			.build();

		Post savedPost = postRepository.save(post);

		List<String> userIdList = applyRepository.findAllByMeetingIdAndStatus(meeting.getId(),
				EnApplyStatus.APPROVE)
			.stream()
			.map(apply -> String.valueOf(apply.getUser().getOrgId()))
			.collect(toList());

		String[] userIds = userIdList.toArray(new String[0]);
		String pushNotificationContent = String.format("[%s의 새 글] : \"%s\"",
			user.getName(), post.getTitle());
		String pushNotificationWeblink = pushWebUrl + "/detail?id=" + meeting.getId();

		PushNotificationRequestDto pushRequestDto = PushNotificationRequestDto.of(userIds,
			NEW_POST_PUSH_NOTIFICATION_TITLE.getValue(),
			pushNotificationContent,
			PUSH_NOTIFICATION_CATEGORY.getValue(), pushNotificationWeblink);

		pushNotificationService.sendPushNotification(pushRequestDto);

		return PostV2CreatePostResponseDto.of(savedPost.getId());
	}

	@Override
	@Transactional(readOnly = true)
	public PostV2GetPostsResponseDto getPosts(PostGetPostsCommand queryCommand, User user) {
		Sort sort = Sort.by(Sort.Direction.ASC, "id");
		Page<PostDetailResponseDto> meetingPostListDtos = postRepository.findPostList(queryCommand,
			new CustomPageable(queryCommand.getPage() - 1, sort), user);

		PageOptionsDto pageOptionsDto = new PageOptionsDto(meetingPostListDtos.getPageable().getPageNumber() + 1,
			meetingPostListDtos.getPageable().getPageSize());
		PageMetaDto pageMetaDto = new PageMetaDto(pageOptionsDto,
			(int)meetingPostListDtos.getTotalElements());

		return PostV2GetPostsResponseDto.of(meetingPostListDtos.getContent(), pageMetaDto);
	}

	/**
	 * 모임 게시글 단건 조회
	 *
	 * @throws 400
	 * @apiNote 모임에 속한 유저만 작성 가능
	 */
	@Override
	@Transactional(readOnly = true)
	public PostDetailBaseDto getPost(Integer userId, Integer postId) {
		return postRepository.findPost(userId, postId);
	}

	@Override
	public void mentionUserInPost(PostV2MentionUserInPostRequestDto requestBody, Integer userId) {
		User user = userRepository.findByIdOrThrow(userId);
		Post post = postRepository.findByIdOrThrow(requestBody.getPostId());

		String pushNotificationContent = String.format("[%s의 글] : \"%s\"",
			user.getName(), post.getTitle());
		String pushNotificationWeblink = pushWebUrl + "/post?id=" + post.getId();

		String[] userOrgIds = requestBody.getOrgIds().stream()
			.map(Object::toString)
			.toArray(String[]::new);

		PushNotificationRequestDto pushRequestDto = PushNotificationRequestDto.of(
			userOrgIds,
			NEW_POST_MENTION_PUSH_NOTIFICATION_TITLE.getValue(),
			pushNotificationContent,
			PUSH_NOTIFICATION_CATEGORY.getValue(),
			pushNotificationWeblink
		);

		pushNotificationService.sendPushNotification(pushRequestDto);
	}

	/**
	 * 모임 게시글 개수 조회
	 *
	 * @apiNote 모든 유저가 조회 가능
	 */
	@Override
	@Transactional(readOnly = true)
	public PostV2GetPostCountResponseDto getPostCount(Integer meetingId) {
		return PostV2GetPostCountResponseDto.of(postRepository.countByMeetingId(meetingId));
	}

	/**
	 * 모임 게시글 삭제
	 *
	 * @throws 403 글 작성자가 아닌 경우
	 * @apiNote 글을 작성한 유저만 삭제 가능
	 */
	@Override
	@Transactional
	public void deletePost(Integer postId, Integer userId) {
		Post post = postRepository.findByIdOrThrow(postId);
		post.isWriter(userId);

		List<Comment> comments = commentRepository.findAllByPostIdOrderByCreatedDate(postId);
		List<Integer> commentIds = comments.stream().map(Comment::getId).toList();

		commentRepository.deleteAllByPostId(postId);
		likeRepository.deleteAllByPostId(postId);
		likeRepository.deleteAllByCommentIdsInQuery(commentIds);

		postRepository.delete(post);
	}

	/**
	 * 모임 게시글 수정
	 *
	 * @throws 400 존재하지 않는 게시글인 경우
	 * @throws 400 업로드하려는 이미지가 10개 초과인 경우
	 * @throws 403 글 작성자가 아닌 경우
	 * @apiNote 글을 작성한 유저만 수정 가능
	 */
	@Override
	@Transactional
	public PostV2UpdatePostResponseDto updatePost(Integer postId, PostV2UpdatePostBodyDto requestBody, Integer userId) {
		Post post = postRepository.findByIdOrThrow(postId);
		post.isWriter(userId);

		if (requestBody.getImages().length > 10) {
			throw new BadRequestException(MAX_IMAGE_UPLOAD_EXCEEDED.getErrorCode());
		}

		post.updatePost(requestBody.getTitle(), requestBody.getContents(), requestBody.getImages());

		return PostV2UpdatePostResponseDto.of(post.getId(), post.getTitle(), post.getContents(),
			String.valueOf(time.now()),
			post.getImages());
	}

	/**
	 * 모임 게시글 신고
	 *
	 * @param postId 신고할 게시글 id
	 * @param userId 신고하는 유저 id
	 * @return 신고 ID
	 * @throws 400 존재하지 않는 게시글인 경우
	 * @throws 400 이미 신고한 게시글인 경우
	 * @apiNote 중복 신고는 되지 않음
	 */
	@Override
	@Transactional
	public PostV2ReportResponseDto reportPost(Integer postId, Integer userId) {
		Post post = postRepository.findByIdOrThrow(postId);

		if (reportRepository.existsByPostIdAndUserId(postId, userId)) {
			throw new BadRequestException(ALREADY_REPORTED_POST.getErrorCode());
		}

		Report report = Report.builder()
			.post(post)
			.postId(postId)
			.userId(userId)
			.build();

		Report savedReport = reportRepository.save(report);

		return PostV2ReportResponseDto.of(savedReport.getId());
	}

	/**
	 * 모임 게시글 좋아요 토글
	 *
	 * @param postId 좋아요 누르는 게시글 id
	 * @param userId 좋아요 누르는 유저 id
	 * @return 해당 게시글을 좋아요 눌렀는지 여부
	 * @apiNote 회원만 할 수 있음, 좋아요 버튼 누르면 삭제했다가 다시 생김
	 */
	@Override
	@Transactional
	public PostV2SwitchPostLikeResponseDto switchPostLike(Integer postId, Integer userId) {

		Post post = postRepository.findByIdOrThrow(postId);

		// 좋아요 취소
		int deletedLikes = likeRepository.deleteByUserIdAndPostId(userId, postId);

		// 취소된 좋아요 정보가 없을 경우
		if (deletedLikes == 0) {
			Like newLike = Like.builder()
				.postId(postId)
				.userId(userId)
				.build();

			likeRepository.save(newLike);

			// 좋아요 개수 증가
			post.increaseLikeCount();

			return PostV2SwitchPostLikeResponseDto.of(true);
		}

		// 취소된 경우 게시글 좋아요 개수를 감소시킴
		post.decreaseLikeCount();

		return PostV2SwitchPostLikeResponseDto.of(false);
	}
}
