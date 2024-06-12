package org.sopt.makers.crew.main.post.v2.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.common.config.TestConfig;
import org.sopt.makers.crew.main.entity.post.PostRepository;
import org.sopt.makers.crew.main.post.v2.dto.query.PostGetPostsCommand;
import org.sopt.makers.crew.main.post.v2.dto.response.PostDetailDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SqlGroup({
        @Sql(value = "/sql/post-repository-test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)

})
public class PostRepositoryTest {
    @Autowired
    private PostRepository postRepository;

    @Test
    void 모임_ID로_필터링해서_게시글_목록_조회() {
        // given
        int page = 1;
        int take = 9;
        Integer meetingId = 1;
        PostGetPostsCommand queryCommand = new PostGetPostsCommand(meetingId, page, take);
        Integer userId = 1;

        // when
        Page<PostDetailDto> postDetailDtos = postRepository.findPostList(queryCommand, PageRequest.of(page - 1, take),
                userId);

        // then
        assertThat(postDetailDtos.getTotalElements()).isEqualTo(3);
        assertThat(postDetailDtos.getSize()).isEqualTo(take);
        assertThat(postDetailDtos.getNumber()).isEqualTo(page - 1);
        assertThat(postDetailDtos.getTotalPages()).isEqualTo(1);

        PostDetailDto postDetailDto1 = postDetailDtos.getContent().get(0);
        assertThat(postDetailDto1)
                .extracting("id", "title", "contents", "createdDate", "likeCount", "isLiked", "commentCount")
                .containsExactly(3, "제목3", "내용3",
                        LocalDateTime.of(LocalDate.of(2024, 06, 11), LocalTime.of(10, 0, 2, 0)),
                        0, false, 0);
        assertThat(postDetailDto1.getMeeting())
                .extracting("id", "title", "category")
                .containsExactly(1, "스터디 구합니다1", "행사");

        PostDetailDto postDetailDto2 = postDetailDtos.getContent().get(1);
        assertThat(postDetailDto2)
                .extracting("id", "title", "contents", "createdDate", "likeCount", "isLiked", "commentCount")
                .containsExactly(2, "제목2", "내용2",
                        LocalDateTime.of(LocalDate.of(2024, 06, 11), LocalTime.of(10, 0, 1, 0)),
                        0, false, 0);
        assertThat(postDetailDto2.getMeeting())
                .extracting("id", "title", "category")
                .containsExactly(1, "스터디 구합니다1", "행사");

        PostDetailDto postDetailDto3 = postDetailDtos.getContent().get(2);
        assertThat(postDetailDto3)
                .extracting("id", "title", "contents", "createdDate", "likeCount", "isLiked", "commentCount")
                .containsExactly(1, "제목1", "내용1",
                        LocalDateTime.of(LocalDate.of(2024, 06, 11), LocalTime.of(10, 0, 0, 0)),
                        2, true, 5);
        assertThat(postDetailDto3.getMeeting())
                .extracting("id", "title", "category")
                .containsExactly(1, "스터디 구합니다1", "행사");
        // 댓글 작성자 최대 3명 순서 검증
        assertThat(postDetailDto3.getCommenterThumbnails())
                .containsExactly("profile1.jpg", "profile2.jpg", "profile3.jpg");
    }

    @Test
    void 게시글_목록_전체_조회() {
        // given
        int page = 1;
        int take = 9;
        PostGetPostsCommand queryCommand = new PostGetPostsCommand(null, page, take);
        Integer userId = 1;

        // when
        Page<PostDetailDto> postDetailDtos = postRepository.findPostList(queryCommand, PageRequest.of(page - 1, take),
                userId);

        // then
        assertThat(postDetailDtos.getTotalElements()).isEqualTo(5);
        assertThat(postDetailDtos.getSize()).isEqualTo(take);
        assertThat(postDetailDtos.getNumber()).isEqualTo(page - 1);
        assertThat(postDetailDtos.getTotalPages()).isEqualTo(1);

        PostDetailDto postDetailDto1 = postDetailDtos.getContent().get(0);
        assertThat(postDetailDto1)
                .extracting("id", "title", "contents", "createdDate", "likeCount", "isLiked", "commentCount")
                .containsExactly(5, "제목5", "내용5",
                        LocalDateTime.of(LocalDate.of(2024, 06, 11), LocalTime.of(10, 0, 4, 0)),
                        0, false, 0);
        assertThat(postDetailDto1.getMeeting())
                .extracting("id", "title", "category")
                .containsExactly(2, "스터디 구합니다2", "스터디");

        PostDetailDto postDetailDto2 = postDetailDtos.getContent().get(4);
        assertThat(postDetailDto2)
                .extracting("id", "title", "contents", "createdDate", "likeCount", "isLiked", "commentCount")
                .containsExactly(1, "제목1", "내용1",
                        LocalDateTime.of(LocalDate.of(2024, 06, 11), LocalTime.of(10, 0, 0, 0)),
                        2, true, 5);
        assertThat(postDetailDto2.getMeeting())
                .extracting("id", "title", "category")
                .containsExactly(1, "스터디 구합니다1", "행사");
    }

}
