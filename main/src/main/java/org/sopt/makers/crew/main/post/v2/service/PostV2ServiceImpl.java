package org.sopt.makers.crew.main.post.v2.service;

import static java.util.stream.Collectors.toList;
import static org.sopt.makers.crew.main.common.response.ErrorStatus.FORBIDDEN_EXCEPTION;
import static org.sopt.makers.crew.main.internal.notification.PushNotificationEnums.NEW_POST_PUSH_NOTIFICATION_TITLE;
import static org.sopt.makers.crew.main.internal.notification.PushNotificationEnums.PUSH_NOTIFICATION_CATEGORY;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.common.exception.ForbiddenException;
import org.sopt.makers.crew.main.common.pagination.dto.PageMetaDto;
import org.sopt.makers.crew.main.common.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.post.Post;
import org.sopt.makers.crew.main.entity.post.PostRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.internal.notification.PushNotificationService;
import org.sopt.makers.crew.main.internal.notification.dto.PushNotificationRequestDto;
import org.sopt.makers.crew.main.post.v2.dto.query.PostGetPostsCommand;
import org.sopt.makers.crew.main.post.v2.dto.request.PostV2CreatePostBodyDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostDetailResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2CreatePostResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2GetPostsResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final PushNotificationService pushNotificationService;

    @Value("${push-notification.web-url}")
    private String pushWebUrl;

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

        boolean isInMeeting = meeting.getAppliedInfo().stream()
                .anyMatch(apply -> apply.getUserId().equals(userId)
                        && apply.getStatus() == EnApplyStatus.APPROVE);

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
    public PostV2GetPostsResponseDto getPosts(PostGetPostsCommand queryCommand, Integer userId) {
        Page<PostDetailResponseDto> meetingPostListDtos = postRepository.findPostList(queryCommand,
                PageRequest.of(queryCommand.getPage() - 1, queryCommand.getTake()), userId);

        PageOptionsDto pageOptionsDto = new PageOptionsDto(queryCommand.getPage(), queryCommand.getTake());
        PageMetaDto pageMetaDto = new PageMetaDto(pageOptionsDto, (int) meetingPostListDtos.getTotalElements());

        return PostV2GetPostsResponseDto.of(meetingPostListDtos.getContent(), pageMetaDto);
    }
}
