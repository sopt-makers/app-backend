package org.sopt.app.interfaces.postgres;

import java.util.List;
import java.util.Optional;
import org.sopt.app.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByNickname(String nickname);

    Optional<User> findUserById(Long userId);

    Optional<User> findUserByPlaygroundId(Long playgroundId);

    List<User> findAllByPlaygroundIdIn(List<Long> playgroundIds);
}
