package org.sopt.makers.crew.main.user.v2;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.common.annotation.IntegratedTest;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllMentionUserDto;
import org.sopt.makers.crew.main.user.v2.service.UserV2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

@IntegratedTest
public class UserServiceTest {

    @Autowired
    private UserV2Service userV2Service;

    @Autowired
    private UserRepository userRepository;


    @Test
    void 멘션_사용자_조회(){
        // given
        User user1 = User.builder()
                .name("홍길동")
                .orgId(1)
                .activities(List.of(new UserActivityVO("서버", 33), new UserActivityVO("iOS", 34)))
                .profileImage("image-url1")
                .phone("010-1234-5678")
                .build();
        User user2 = User.builder()
                .name("김철수")
                .orgId(2)
                .activities(List.of(new UserActivityVO("iOS", 30), new UserActivityVO("안드로이드", 33)))
                .profileImage("image-url2")
                .phone("010-1111-2222")
                .build();
        userRepository.saveAll(List.of(user1, user2));

        // when
        List<UserV2GetAllMentionUserDto> allMentionUsers = userV2Service.getAllMentionUser();

        // then
        assertThat(allMentionUsers).hasSize(2);
        assertThat(allMentionUsers.get(0))
                .extracting( "userName", "recentPart", "recentGeneration", "profileImageUrl")
                .containsExactly( "홍길동", "iOS", 34, "image-url1");
        assertThat(allMentionUsers.get(1))
                .extracting("userName", "recentPart", "recentGeneration", "profileImageUrl")
                .containsExactly("김철수", "안드로이드", 33, "image-url2");

    }

    @Test
    void 멘션_사용자_조회시_db에_null_저장된_경우(){
        // given
        User user1 = User.builder()
                .name("홍길동")
                .orgId(1)
                .activities(null)
                .profileImage("image-url1")
                .phone("010-1234-5678")
                .build();
        User user2 = User.builder()
                .name("김철수")
                .orgId(2)
                .activities(List.of(new UserActivityVO("iOS", 30), new UserActivityVO("안드로이드", 33)))
                .profileImage("image-url2")
                .phone("010-1111-2222")
                .build();
        userRepository.saveAll(List.of(user1, user2));

        // when
        List<UserV2GetAllMentionUserDto> allMentionUsers = userV2Service.getAllMentionUser();

        // then
        assertThat(allMentionUsers).hasSize(1);
        assertThat(allMentionUsers.get(0))
                .extracting( "userName", "recentPart", "recentGeneration", "profileImageUrl")
                .containsExactly("김철수", "안드로이드", 33, "image-url2");
    }

    @Test
    void 멘션_사용자_조회시_db에_올바르지_않은_데이터_저장된_경우(){
        // given
        User user1 = User.builder()
                .name("홍길동")
                .orgId(1)
                .activities(List.of(new UserActivityVO("서버", 33), new UserActivityVO("iOS", 34)))
                .profileImage("image-url1")
                .phone("010-1234-5678")
                .build();
        User user2 = User.builder()
                .name("김철수")
                .orgId(2)
                .activities(List.of(new UserActivityVO(null, 30), new UserActivityVO("", 34)))
                .profileImage("image-url2")
                .phone("010-1111-2222")
                .build();
        userRepository.saveAll(List.of(user1, user2));

        // when
        List<UserV2GetAllMentionUserDto> allMentionUsers = userV2Service.getAllMentionUser();

        // then
        assertThat(allMentionUsers).hasSize(2);
        assertThat(allMentionUsers.get(0))
                .extracting("userName", "recentPart", "recentGeneration", "profileImageUrl")
                .containsExactly("홍길동", "iOS", 34, "image-url1");
        assertThat(allMentionUsers.get(1))
                .extracting("userName", "recentPart", "recentGeneration", "profileImageUrl")
                .containsExactly("김철수", "", 34, "image-url2");
    }
}
