package org.sopt.app.facade;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.sopt.app.application.rank.SoptampPartRankCalculator;
import org.sopt.app.application.soptamp.SoptampPointInfo.Main;
import org.sopt.app.application.soptamp.SoptampPointInfo.PartRank;
import org.sopt.app.application.soptamp.SoptampUserFinder;
import org.sopt.app.application.soptamp.SoptampUserInfo;
import org.sopt.app.domain.enums.Part;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RankFacade {

    private final SoptampUserFinder soptampUserFinder;

    @Transactional(readOnly = true)
    public List<Main> findCurrentRanks() {
        AtomicInteger rankPoint = new AtomicInteger(1);
        return soptampUserFinder.findAllOfCurrentGenerationOrderByTotalPoints().stream()
                .map(user -> Main.of(rankPoint.getAndIncrement(), user))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Main> findCurrentRanksByPart(Part part) {
        AtomicInteger rankPoint = new AtomicInteger(1);
        return soptampUserFinder.findAllByPartAndCurrentGenerationOrderByTotalPoints(part).stream()
                .map(user -> Main.of(rankPoint.getAndIncrement(), user))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PartRank> findAllPartRanks() {
        List<SoptampUserInfo> soptampUserInfos = soptampUserFinder.findAllOfCurrentGeneration();
        SoptampPartRankCalculator soptampPartRankCalculator = new SoptampPartRankCalculator(soptampUserInfos);
        return soptampPartRankCalculator.calculatePartRank();
    }
}
