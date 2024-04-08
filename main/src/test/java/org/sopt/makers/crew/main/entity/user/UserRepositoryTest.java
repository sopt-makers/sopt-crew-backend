package org.sopt.makers.crew.main.entity.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void 유저저장_성공(){
        // given
        User user = UserFixture.createStaticUser();

        // when
        User savedUser = userRepository.save(user);

        // then
        Assertions.assertThat(savedUser)
                .extracting("name", "phone")
                .containsExactly(user.getName(), user.getPhone());
    }
}
