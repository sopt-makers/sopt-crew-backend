package org.sopt.makers.crew.main.post.v2.service;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDateTime;
import java.time.Month;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.common.exception.BadRequestException;
import org.sopt.makers.crew.main.common.exception.ForbiddenException;
import org.sopt.makers.crew.main.common.util.Time;
import org.sopt.makers.crew.main.entity.like.LikeRepository;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.post.Post;
import org.sopt.makers.crew.main.entity.post.PostRepository;
import org.sopt.makers.crew.main.entity.report.Report;
import org.sopt.makers.crew.main.entity.report.ReportRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserFixture;
import org.sopt.makers.crew.main.post.v2.dto.request.PostV2UpdatePostBodyDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2ReportResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2SwitchPostLikeResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2UpdatePostResponseDto;

@ExtendWith(MockitoExtension.class)
public class PostV2ServiceTest {

    @InjectMocks
    private PostV2ServiceImpl postV2Service;
    @Mock
    private PostRepository postRepository;
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private LikeRepository likeRepository;

    @Mock
    private Time time;

    private User user;

    private Meeting meeting;

    private Post post;

    private Report report;

    @BeforeEach
    void init() {
        user = UserFixture.createStaticUser();
        user.setUserIdForTest(1);

        meeting = Meeting.builder()
                .user(user)
                .userId(user.getId())
                .title("사람 구해요")
                .category(MeetingCategory.STUDY)
                .startDate(LocalDateTime.of(2024, Month.MARCH, 17, 0, 0))
                .endDate(LocalDateTime.of(2024, Month.MARCH, 20, 23, 59))
                .capacity(10)
                .desc("열정 많은 사람 구해요")
                .processDesc("이렇게 할거에여")
                .mStartDate(LocalDateTime.of(2024, Month.APRIL, 1, 0, 0))
                .mEndDate(LocalDateTime.of(2030, Month.APRIL, 20, 0, 0))
                .leaderDesc("저는 이런 사람이에요.")
                .targetDesc("이런 사람이 왔으면 좋겠어요")
                .note("유의사항은 이거에요")
                .isMentorNeeded(true)
                .canJoinOnlyActiveGeneration(true)
                .createdGeneration(33)
                .targetActiveGeneration(33)
                .joinableParts(MeetingJoinablePart.values())
                .build();

        String[] images = {"image1", "image2", "image3"};
        post = Post.builder().user(user).title("title").contents("contents").images(images).meeting(meeting).build();
        report = Report.builder().post(post).postId(post.getId()).userId(user.getId()).build();

    }

    @Nested
    class 게시글_수정 {

        @Test
        void 성공() {
            // given
            String[] images = {"image4", "image5"};
            PostV2UpdatePostBodyDto requestDto = new PostV2UpdatePostBodyDto("글 제목 수정", "글 내용 수정", images);

            doReturn(post).when(postRepository).findByIdOrThrow(any());
            doReturn(LocalDateTime.of(2024, 4, 24, 23, 59)).when(time).now();

            // when
            PostV2UpdatePostResponseDto result = postV2Service.updatePost(post.getId(),
                    requestDto, user.getId());

            // then
            Assertions.assertThat(result.getId()).isEqualTo(post.getId());
            Assertions.assertThat(result.getContents()).isEqualTo(requestDto.getContents());
            Assertions.assertThat(LocalDateTime.parse(result.getUpdatedDate()))
                    .isEqualTo(LocalDateTime.of(2024, 4, 24, 23, 59));
        }

        @Test
        void 실패_본인_작성_게시글_아님() {
            // given
            doReturn(post).when(postRepository).findByIdOrThrow(any());
            String[] images = {"image4", "image5"};
            PostV2UpdatePostBodyDto requestDto = new PostV2UpdatePostBodyDto("글 제목 수정", "글 내용 수정", images);

            // when & then
            assertThrows(ForbiddenException.class, () -> {
                postV2Service.updatePost(post.getId(), requestDto,
                        post.getUserId() + 1);
            });
        }
    }

    @Nested
    class 게시글_신고 {

        @Test
        void 게시글_신고_성공() {
            // given
            doReturn(post).when(postRepository).findByIdOrThrow(any());
            doReturn(false).when(reportRepository).existsByPostIdAndUserId(any(), any());
            doReturn(report).when(reportRepository).save(any());

            // when
            PostV2ReportResponseDto result = postV2Service.reportPost(post.getId(),
                    user.getId());

            // then
            Assertions.assertThat(result.getReportId()).isEqualTo(report.getId());
        }

        @Test
        void 댓글_신고_실패_이미_신고한_댓글() {
            // given
            doReturn(post).when(postRepository).findByIdOrThrow(any());
            doReturn(true).when(reportRepository).existsByPostIdAndUserId(any(), any());

            // when & then
            assertThrows(BadRequestException.class, () -> {
                postV2Service.reportPost(post.getId(), user.getId());
            });
        }
    }

    @Nested
    class 게시글_좋아요_토글 {

        @Test
        void 기존에_게시글_좋아요_안눌렀을때_좋아요_누르기_성공() {
            // given
            doReturn(post).when(postRepository).findByIdOrThrow(any());
            doReturn(0).when(likeRepository).deleteByUserIdAndPostId(any(), any()); //기존에 좋아요 누른 적 없을 때

            //when
            PostV2SwitchPostLikeResponseDto result = postV2Service.switchPostLike(post.getId(), user.getId());

            //then
            Assertions.assertThat(result.getIsLiked()).isEqualTo(true);
            Assertions.assertThat(post.getLikeCount()).isEqualTo(1);
        }

        @Test
        void 기존에_게시글_좋아요_눌렀을때_좋아요_취소_성공() {
            // given
            post.increaseLikeCount();
            doReturn(post).when(postRepository).findByIdOrThrow(any());
            doReturn(1).when(likeRepository).deleteByUserIdAndPostId(any(), any()); //기존에 좋아요 누른 적 있을 때

            //when
            PostV2SwitchPostLikeResponseDto result = postV2Service.switchPostLike(post.getId(), user.getId());

            //then
            Assertions.assertThat(result.getIsLiked()).isEqualTo(false);
            Assertions.assertThat(post.getLikeCount()).isEqualTo(0);
        }
    }

}
