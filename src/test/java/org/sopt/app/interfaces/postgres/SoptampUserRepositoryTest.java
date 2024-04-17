package org.sopt.app.interfaces.postgres;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.app.common.config.QuerydslConfiguration;
import org.sopt.app.domain.entity.SoptampUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(QuerydslConfiguration.class)
class SoptampUserRepositoryTest {

    @Autowired
    private SoptampUserRepository soptampUserRepository;

    SoptampUser user = new SoptampUser();

    @BeforeEach
    void beforeTest() {

        user = soptampUserRepository.save(
                SoptampUser.builder()
                        .userId(generateUniqueUserId())
                        .nickname(generateUniqueNickname())
                        .build()
        );
    }

    private Long generateUniqueUserId() {
        long userId = Long.MAX_VALUE;

        for (int i = 0; i < 1000; i++) {
            if (soptampUserRepository.findByUserId(userId).isEmpty()) {
                break;
            }
            userId = userId - i;
        }
        return userId;
    }

    private String generateUniqueNickname() {
        String nickname = "uniqueNickname";
        long uniqueNumber = Long.MAX_VALUE;

        for (int i = 0; i < 1000; i++) {
            if (soptampUserRepository.findUserByNickname(nickname).isEmpty()) {
                break;
            }
            nickname = "uniqueNickname" + (uniqueNumber - i);
        }
        return nickname;
    }

    @Test
    @DisplayName("SUCCESS_유저 아이디로 유저 찾기")
    void SUCCESS_findByUserId() {
        Assertions.assertEquals(Optional.of(user), soptampUserRepository.findByUserId(user.getUserId()));
    }

    @Test
    @DisplayName("SUCCESS_유저 닉네임으로 유저 찾기")
    void findUserByNickname() {
        Assertions.assertEquals(Optional.of(user), soptampUserRepository.findUserByNickname(user.getNickname()));
    }
}