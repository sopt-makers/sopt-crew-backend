package org.sopt.makers.crew.main.entity.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@SqlGroup({
    @Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void 유저저장_성공(){
        // given
        User user = UserFixture.createStaticUser();

        // when
        User savedUser = userRepository.save(user);
        User foundUser = userRepository.findByIdOrThrow(savedUser.getId());

        // then
        Assertions.assertThat(foundUser)
                .extracting("name", "phone")
                .containsExactly(user.getName(), user.getPhone());
    }
}
