package org.sopt.makers.crew.main.post.v2.service;

import static java.util.stream.Collectors.*;
import static org.sopt.makers.crew.main.common.exception.ErrorStatus.*;
import static org.sopt.makers.crew.main.internal.notification.PushNotificationEnums.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.sopt.makers.crew.main.external.playground.service.MemberBlockService;
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
import org.sopt.makers.crew.main.user.v2.service.UserV2Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

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
	private final MemberBlockService memberBlockService;
	private final UserV2Service userV2Service;

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
	public PostV2CreatePostResponseDto createPost(PostV2CreatePostBodyDto requestBody, Integer userId) {
		Meeting meeting = meetingRepository.findByIdOrThrow(requestBody.getMeetingId());
		User user = userRepository.findByIdOrThrow(userId);

		List<Apply> applies = applyRepository.findAllByMeetingId(meeting.getId());

		boolean isInMeeting = applies.stream()
			.anyMatch(apply -> apply.getUserId().equals(userId) && apply.getStatus().equals(EnApplyStatus.APPROVE));

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

		List<String> userIdList = applyRepository.findAllByMeetingIdAndStatus(meeting.getId(), EnApplyStatus.APPROVE)
			.stream()
			.map(apply -> String.valueOf(apply.getUser().getOrgId()))
			.collect(toList());

		String[] userIds = userIdList.toArray(new String[0]);
		String pushNotificationContent = String.format("[%s의 새 글] : \"%s\"", user.getName(), post.getTitle());
		String pushNotificationWeblink = pushWebUrl + "/detail?id=" + meeting.getId();

		PushNotificationRequestDto pushRequestDto = PushNotificationRequestDto.of(userIds,
			NEW_POST_PUSH_NOTIFICATION_TITLE.getValue(), pushNotificationContent, PUSH_NOTIFICATION_CATEGORY.getValue(),
			pushNotificationWeblink);

		pushNotificationService.sendPushNotification(pushRequestDto);

		return PostV2CreatePostResponseDto.of(savedPost.getId());
	}

	/**
	 * 모일 게시글 리스트 페이지네이션 조회 (12개)
	 *
	 * @param queryCommand 게시글 조회를 위한 쿼리 명령 객체
	 * @param userId 게시글을 조회하는 사용자 id
	 * @return 게시글 정보(게시글 객체 + 댓글 단 사람의 썸네일 + 차단된 유저의 게시물 여부)와 페이지 메타 정보를 포함한 응답 DTO
	 * @apiNote 사용자가 차단한 유저의 게시물은 해당 게시물에 대한 차단 여부를 함께 반환
	 */
	@Override
	@Transactional(readOnly = true)
	public PostV2GetPostsResponseDto getPosts(PostGetPostsCommand queryCommand, Integer userId) {
		Sort sort = Sort.by(Sort.Direction.ASC, "id");
		Page<PostDetailResponseDto> meetingPostListDtos = postRepository.findPostList(queryCommand,
			new CustomPageable(queryCommand.getPage() - 1, sort), userId);

		PageOptionsDto pageOptionsDto = new PageOptionsDto(meetingPostListDtos.getPageable().getPageNumber() + 1,
			meetingPostListDtos.getPageable().getPageSize());
		PageMetaDto pageMetaDto = new PageMetaDto(pageOptionsDto, (int)meetingPostListDtos.getTotalElements());

		List<Long> userOrgIds = meetingPostListDtos.getContent()
			.stream()
			.map(postDetail -> postDetail.getUser().getOrgId().longValue())
			.collect(Collectors.toList());

		User user = userV2Service.getUserByUserId(userId);
		Long orgId = user.getOrgId().longValue();

		Map<Long, Boolean> blockedPostMap = memberBlockService.getBlockedUsers(orgId, userOrgIds);

		List<PostDetailResponseDto> responseDtos = meetingPostListDtos.getContent()
			.stream()
			.map(postDetail -> toPostDetailResponseDto(postDetail, blockedPostMap))
			.collect(Collectors.toList());

		return PostV2GetPostsResponseDto.of(responseDtos, pageMetaDto);
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

		String pushNotificationContent = String.format("[%s의 글] : \"%s\"", user.getName(), post.getTitle());
		String pushNotificationWeblink = pushWebUrl + "/post?id=" + post.getId();

		String[] userOrgIds = requestBody.getOrgIds().stream().map(Object::toString).toArray(String[]::new);

		PushNotificationRequestDto pushRequestDto = PushNotificationRequestDto.of(userOrgIds,
			NEW_POST_MENTION_PUSH_NOTIFICATION_TITLE.getValue(), pushNotificationContent,
			PUSH_NOTIFICATION_CATEGORY.getValue(), pushNotificationWeblink);

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
			String.valueOf(time.now()), post.getImages());
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

		Report report = Report.builder().post(post).postId(postId).userId(userId).build();

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
			Like newLike = Like.builder().postId(postId).userId(userId).build();

			likeRepository.save(newLike);

			// 좋아요 개수 증가
			post.increaseLikeCount();

			return PostV2SwitchPostLikeResponseDto.of(true);
		}

		// 취소된 경우 게시글 좋아요 개수를 감소시킴
		post.decreaseLikeCount();

		return PostV2SwitchPostLikeResponseDto.of(false);
	}

	private PostDetailResponseDto toPostDetailResponseDto(PostDetailResponseDto postDetail,
		Map<Long, Boolean> blockedPostMap) {
		boolean isBlockedPost = blockedPostMap.getOrDefault(postDetail.getUser().getOrgId().longValue(), false);
		return PostDetailResponseDto.of(
			postDetail.getId(),
			postDetail.getTitle(),
			postDetail.getContents(),
			postDetail.getCreatedDate(),
			postDetail.getImages(),
			postDetail.getUser(),
			postDetail.getLikeCount(),
			postDetail.getIsLiked(),
			postDetail.getViewCount(),
			postDetail.getCommentCount(),
			postDetail.getMeeting(),
			postDetail.getCommenterThumbnails(),
			isBlockedPost
		);
	}
}
