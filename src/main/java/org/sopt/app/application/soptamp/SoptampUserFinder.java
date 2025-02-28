package org.sopt.app.application.soptamp;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.app.common.exception.BadRequestException;
import org.sopt.app.common.response.ErrorCode;
import org.sopt.app.domain.entity.soptamp.SoptampUser;
import org.sopt.app.domain.enums.Part;
import org.sopt.app.interfaces.postgres.SoptampUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SoptampUserFinder {

    private final SoptampUserRepository soptampUserRepository;

    @Value("${sopt.current.generation}")
    private Long currentGeneration;

    public List<SoptampUserInfo> findAllOfCurrentGeneration() {
        return soptampUserRepository.findAllByGeneration(currentGeneration)
                .stream()
                .map(SoptampUserInfo::of)
                .toList();
    }

    public List<SoptampUserInfo> findAllOfCurrentGenerationOrderByTotalPoints() {
        return soptampUserRepository.findAllByGenerationOrderByTotalPointsDesc(currentGeneration)
                .stream()
                .map(SoptampUserInfo::of)
                .toList();
    }

    public List<SoptampUserInfo> findAllByPartAndCurrentGenerationOrderByTotalPoints(Part part) {
        return soptampUserRepository.findAllByNicknameStartingWithAndGenerationOrderByTotalPointsDesc(part.getPartName(), currentGeneration)
                .stream()
                .map(SoptampUserInfo::of)
                .toList();
    }

    public SoptampUserInfo findByNickname(String nickname) {
        SoptampUser soptampUser = soptampUserRepository.findUserByNickname(nickname)
                .orElseThrow(() -> new BadRequestException(ErrorCode.USER_NOT_FOUND));
        return SoptampUserInfo.of(soptampUser);
    }
}
