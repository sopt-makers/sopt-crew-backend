package org.sopt.makers.crew.main.comment.v2.service;

import static org.sopt.makers.crew.main.internal.notification.PushNotificationEnums.NEW_COMMENT_MENTION_PUSH_NOTIFICATION_TITLE;
import static org.sopt.makers.crew.main.internal.notification.PushNotificationEnums.NEW_COMMENT_PUSH_NOTIFICATION_TITLE;
import static org.sopt.makers.crew.main.internal.notification.PushNotificationEnums.PUSH_NOTIFICATION_CATEGORY;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.comment.v2.dto.request.CommentV2CreateCommentBodyDto;
import org.sopt.makers.crew.main.comment.v2.dto.request.CommentV2MentionUserInCommentRequestDto;
import org.sopt.makers.crew.main.comment.v2.dto.response.CommentV2CreateCommentResponseDto;
import org.sopt.makers.crew.main.comment.v2.dto.response.CommentV2ReportCommentResponseDto;
import org.sopt.makers.crew.main.common.exception.BadRequestException;
import org.sopt.makers.crew.main.common.exception.ForbiddenException;
import org.sopt.makers.crew.main.common.response.ErrorStatus;
import org.sopt.makers.crew.main.entity.comment.Comment;
import org.sopt.makers.crew.main.entity.comment.CommentRepository;
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

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;
    private final PushNotificationService pushNotificationService;

    @Value("${push-notification.web-url}")
    private String pushWebUrl;

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
        User user = userRepository.findByIdOrThrow(userId);

        Comment comment = Comment.builder()
            .contents(requestBody.getContents())
            .user(user)
            .post(post)
            .build();

        Comment savedComment = commentRepository.save(comment);

        User PostWriter = post.getUser();
        String[] userIds = {String.valueOf(PostWriter.getOrgId())};

        String pushNotificationContent = String.format("[%s의 댓글] : \"%s\"",
            user.getName(), requestBody.getContents());
        String pushNotificationWeblink = pushWebUrl + "/post?id=" + post.getId();

        PushNotificationRequestDto pushRequestDto = PushNotificationRequestDto.of(userIds,
            NEW_COMMENT_PUSH_NOTIFICATION_TITLE.getValue(),
            pushNotificationContent,
            PUSH_NOTIFICATION_CATEGORY.getValue(), pushNotificationWeblink);

        pushNotificationService.sendPushNotification(pushRequestDto);

        return CommentV2CreateCommentResponseDto.of(savedComment.getId());
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

        Post post = comment.getPost();

        post.decreaseCommentCount();
        commentRepository.delete(comment);
    }

    @Override
    public void mentionUserInComment(CommentV2MentionUserInCommentRequestDto requestBody,
        Integer userId) {
        User user = userRepository.findByIdOrThrow(userId);
        Post post = postRepository.findByIdOrThrow(requestBody.getPostId());

        String pushNotificationContent = String.format("[%s님이 회원님을 언급했어요.] : \"%s\"",
            user.getName(), requestBody.getContent());
        String pushNotificationWeblink = pushWebUrl + "/post?id=" + post.getId();

        String[] userIdsArray = requestBody.getUserIds().stream()
            .map(userRepository::findByIdOrThrow)
            .map(mentionedUser -> String.valueOf(mentionedUser.getOrgId()))
            .toArray(String[]::new);

        String newCommentMentionPushNotificationTitle = String.format(
            NEW_COMMENT_MENTION_PUSH_NOTIFICATION_TITLE.getValue(), user.getName());

        PushNotificationRequestDto pushRequestDto = PushNotificationRequestDto.of(
            userIdsArray,
            newCommentMentionPushNotificationTitle,
            pushNotificationContent,
            PUSH_NOTIFICATION_CATEGORY.getValue(),
            pushNotificationWeblink
        );

        pushNotificationService.sendPushNotification(pushRequestDto);
    }
}
