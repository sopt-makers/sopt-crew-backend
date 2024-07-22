package org.sopt.makers.crew.main.comment.v2.service;

import static org.sopt.makers.crew.main.internal.notification.PushNotificationEnums.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.comment.v2.dto.CommentMapper;
import org.sopt.makers.crew.main.comment.v2.dto.request.CommentV2CreateCommentBodyDto;
import org.sopt.makers.crew.main.comment.v2.dto.request.CommentV2MentionUserInCommentRequestDto;
import org.sopt.makers.crew.main.comment.v2.dto.response.CommentDto;
import org.sopt.makers.crew.main.comment.v2.dto.response.CommentV2CreateCommentResponseDto;
import org.sopt.makers.crew.main.comment.v2.dto.response.CommentV2GetCommentsResponseDto;
import org.sopt.makers.crew.main.comment.v2.dto.response.CommentV2ReportCommentResponseDto;
import org.sopt.makers.crew.main.comment.v2.dto.response.CommentV2UpdateCommentResponseDto;
import org.sopt.makers.crew.main.common.exception.BadRequestException;
import org.sopt.makers.crew.main.common.exception.ForbiddenException;
import org.sopt.makers.crew.main.common.response.ErrorStatus;
import org.sopt.makers.crew.main.common.util.MentionSecretStringRemover;
import org.sopt.makers.crew.main.common.util.Time;
import org.sopt.makers.crew.main.entity.comment.Comment;
import org.sopt.makers.crew.main.entity.comment.CommentRepository;
import org.sopt.makers.crew.main.entity.like.LikeRepository;
import org.sopt.makers.crew.main.entity.like.MyLikes;
import org.sopt.makers.crew.main.entity.post.Post;
import org.sopt.makers.crew.main.entity.post.PostRepository;
import org.sopt.makers.crew.main.entity.report.Report;
import org.sopt.makers.crew.main.entity.report.ReportRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.internal.notification.PushNotificationService;
import org.sopt.makers.crew.main.internal.notification.dto.PushNotificationRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentV2ServiceImpl implements CommentV2Service {

    private static final int IS_PARENT_COMMENT = 0;
    private static final int IS_REPLY_COMMENT = 1;
    private static final String DELETE_COMMENT_CONTENT = "삭제된 댓글입니다.";

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;
    private final LikeRepository likeRepository;

    private final PushNotificationService pushNotificationService;

    private final CommentMapper commentMapper;

    @Value("${push-notification.web-url}")
    private String pushWebUrl;

    private final Time time;

    /**
     * 모임 게시글 댓글 작성
     *
     * @throws 400 존재하지 않는 게시글일 떄
     * @apiNote 모임에 속한 유저만 작성 가능
     */
    @Override
    @Transactional
    public CommentV2CreateCommentResponseDto createComment(
        CommentV2CreateCommentBodyDto requestBody,
        Integer userId) {
        Post post = postRepository.findByIdOrThrow(requestBody.getPostId());
        User writer = userRepository.findByIdOrThrow(userId);

        int depth = 0;
        int order = 0;
        Integer parentId = 0;

        boolean isReplyComment = !requestBody.isParent();
        if (isReplyComment) {
            validateParentCommentId(requestBody);
            depth = 1;
            parentId = requestBody.getParentCommentId();
            order = getOrder(parentId);
        }

        Comment comment = commentMapper.toComment(requestBody, post, writer, depth, order,
            parentId);

        Comment savedComment = commentRepository.save(comment);
        post.increaseCommentCount();

        sendPushNotification(requestBody, post, writer);

        return CommentV2CreateCommentResponseDto.of(savedComment.getId());
    }

    private void sendPushNotification(CommentV2CreateCommentBodyDto requestBody, Post post,
        User user) {
        User PostWriter = post.getUser();
        String[] userIds = {String.valueOf(PostWriter.getOrgId())};
        String secretStringRemovedContent = MentionSecretStringRemover.removeSecretString(
            requestBody.getContents());
        String pushNotificationContent = String.format("[%s의 댓글] : \"%s\"",
            user.getName(), secretStringRemovedContent);
        String pushNotificationWeblink = pushWebUrl + "/post?id=" + post.getId();

        PushNotificationRequestDto pushRequestDto = PushNotificationRequestDto.of(userIds,
            NEW_COMMENT_PUSH_NOTIFICATION_TITLE.getValue(),
            pushNotificationContent,
            PUSH_NOTIFICATION_CATEGORY.getValue(), pushNotificationWeblink);

        pushNotificationService.sendPushNotification(pushRequestDto);
    }

    private void validateParentCommentId(CommentV2CreateCommentBodyDto requestBody) {
        commentRepository.findByIdAndPostIdOrThrow(requestBody.getParentCommentId(),
            requestBody.getPostId());
    }

    private int getOrder(Integer parentId) {
        Optional<Comment> recentComment = commentRepository.findFirstByParentIdOrderByOrderDesc(
            parentId);
        return recentComment.map(comment -> comment.getOrder() + 1).orElse(1);
    }

    /**
     * 모임 게시글 댓글 수정
     *
     * @param commentId 수정할 댓글 ID
     * @param contents  수정할 내용
     * @param userId    수정하는 유저 ID
     * @return 수정된 댓글 정보
     */
    @Override
    @Transactional
    public CommentV2UpdateCommentResponseDto updateComment(Integer commentId,
        String contents, Integer userId) {
        // 1. id를 기반으로 comment를 찾는다.
        Comment comment = commentRepository.findByIdOrThrow(commentId);

        // 2. comment의 user_id와 userId가 같은지 확인한다.
        comment.validateWriter(userId);

        // 3. comment의 contents를 수정한다.
        comment.updateContents(contents, time.now());

        // 4. 수정된 comment의 id, contents, updatedDate를 반환한다.
        return CommentV2UpdateCommentResponseDto.of(comment.getId(), comment.getContents(),
            String.valueOf(comment.getUpdatedDate()));
    }

    @Override
    public CommentV2GetCommentsResponseDto getComments(Integer postId, Integer page, Integer take,
        Integer userId) {
        // TODO : 페이지네이션 구현

        List<Comment> comments = commentRepository.findAllByPostIdOrderByCreatedDate(postId);

        MyLikes myLikes = new MyLikes(likeRepository.findAllByUserIdAndPostIdNotNull(userId));

        Map<Integer, List<CommentDto>> replyMap = new HashMap<>();
        comments.stream()
            .filter(comment -> !comment.isParentComment())
            .forEach(
                comment -> replyMap.computeIfAbsent(comment.getParentId(), k -> new ArrayList<>())
                    .add(CommentDto.of(comment, myLikes.isLikeComment(comment.getId()),
                        comment.isWriter(userId), null)));

        List<CommentDto> commentDtos = comments.stream()
            .filter(Comment::isParentComment)
            .map(comment -> CommentDto.of(comment, myLikes.isLikeComment(comment.getId()),
                comment.isWriter(userId),
                replyMap.get(comment.getId())))
            .toList();

        return CommentV2GetCommentsResponseDto.of(commentDtos);
    }

    /**
     * 댓글 신고하기
     *
     * @param commentId 댓글 신고할 댓글 id
     * @param userId    신고하는 유저 id
     * @return 신고 ID
     * @throws BadRequestException 이미 신고한 댓글일 때
     * @apiNote 댓글 신고는 한 댓글당 한번만 가능
     */
    @Override
    @Transactional
    public CommentV2ReportCommentResponseDto reportComment(Integer commentId, Integer userId)
        throws BadRequestException {
        Comment comment = commentRepository.findByIdOrThrow(commentId);
        User user = userRepository.findByIdOrThrow(userId);

        Optional<Report> existingReport = reportRepository.findByCommentAndUser(comment, user);

        if (existingReport.isPresent()) {
            throw new BadRequestException(ErrorStatus.ALREADY_REPORTED_COMMENT.getErrorCode());
        }

        Report report = Report.builder()
            .comment(comment)
            .user(user)
            .build();

        Report savedReport = reportRepository.save(report);

        return CommentV2ReportCommentResponseDto.of(savedReport.getId());
    }

    /**
     * 모임 게시글 댓글 삭제
     *
     * @throws ForbiddenException 댓글 작성자가 아닐 때
     * @apiNote 댓글 삭제시 게시글의 댓글 수를 1 감소시킴
     */
    @Override
    @Transactional
    public void deleteComment(Integer commentId, Integer userId) throws ForbiddenException {
        Comment comment = commentRepository.findByIdOrThrow(commentId);

        if (!comment.getUserId().equals(userId)) {
            throw new ForbiddenException();
        }

        Post post = postRepository.findByIdOrThrow(comment.getPostId());
        post.decreaseCommentCount();

        Optional<Comment> childComment = commentRepository.findFirstByParentIdOrderByOrderDesc(
            comment.getId());

        if (comment.getDepth() == IS_REPLY_COMMENT || childComment.isEmpty()) {
            commentRepository.delete(comment);
            return;
        }

        comment.deleteParentComment(DELETE_COMMENT_CONTENT, null, null);
    }

    @Override
    public void mentionUserInComment(CommentV2MentionUserInCommentRequestDto requestBody,
        Integer userId) {
        User user = userRepository.findByIdOrThrow(userId);
        Post post = postRepository.findByIdOrThrow(requestBody.getPostId());

        String pushNotificationContent = NEW_COMMENT_MENTION_PUSH_NOTIFICATION_CONTENT.getValue();
        String pushNotificationWeblink = pushWebUrl + "/post?id=" + post.getId();

        String[] userOrgIds = requestBody.getUserIds().stream()
            .map(Object::toString)
            .toArray(String[]::new);

        String newCommentMentionPushNotificationTitle = String.format(
            NEW_COMMENT_MENTION_PUSH_NOTIFICATION_TITLE.getValue(), user.getName());

        PushNotificationRequestDto pushRequestDto = PushNotificationRequestDto.of(
            userOrgIds,
            newCommentMentionPushNotificationTitle,
            pushNotificationContent,
            PUSH_NOTIFICATION_CATEGORY.getValue(),
            pushNotificationWeblink
        );

        pushNotificationService.sendPushNotification(pushRequestDto);
    }
}
