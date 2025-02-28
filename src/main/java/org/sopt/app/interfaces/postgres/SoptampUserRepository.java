package org.sopt.app.interfaces.postgres;

import java.util.List;
import java.util.Optional;
import org.sopt.app.domain.entity.soptamp.SoptampUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoptampUserRepository extends JpaRepository<SoptampUser, Long> {

    Optional<SoptampUser> findByUserId(Long userId);

    Optional<SoptampUser> findUserByNickname(String nickname);

    List<SoptampUser> findAllByNicknameStartingWithAndGenerationOrderByTotalPointsDesc(String part, Long generation);

    List<SoptampUser> findAllByGeneration(Long generation);

    List<SoptampUser> findAllByGenerationOrderByTotalPointsDesc(Long generation);

    boolean existsByNickname(String nickname);
}
