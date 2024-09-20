package org.sopt.app.application.playground.user_finder;

import static org.sopt.app.application.playground.PlaygroundHeaderCreator.createAuthorizationHeaderByInternalPlaygroundToken;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.sopt.app.application.playground.PlaygroundClient;
import org.sopt.app.application.playground.dto.PlaygroundUserFindCondition;
import org.sopt.app.domain.entity.RecommendedUserIds;
import org.sopt.app.interfaces.postgres.RecommendedUserIdsRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaygroundUserFinderRedisImpl implements PlaygroundUserFinder {

    private final PlaygroundClient playgroundClient;
    private final RecommendedUserIdsRepository recommendedUserIdsRepository;

    @Override
    @Deprecated
    public List<Long> getPlaygroundUserIdsForSameRecommendType(final PlaygroundUserFindCondition request) {
        return List.of();
    }

    @Override
    public Set<Long> findByCondition(PlaygroundUserFindCondition condition) {
        String key = convertConditionToKey(condition);
        Optional<RecommendedUserIds> recommendedUsers = recommendedUserIdsRepository.findById(key);

        if (recommendedUsers.isPresent()) { // 캐싱 되어 있는 조건이라면 캐싱된 값을 반환
            return recommendedUsers.get().getUserIds();
        }

        return this.cachingConditionAndResult(condition);
    }

    private String convertConditionToKey(PlaygroundUserFindCondition request) {
        return request.toString();
    }

    private Set<Long> cachingConditionAndResult(PlaygroundUserFindCondition condition) {
        Map<String, String> headers = createAuthorizationHeaderByInternalPlaygroundToken();
        Set<Long> playgroundUserIds = playgroundClient.getPlaygroundUserIdsByCondition(headers, condition).userIds();
        recommendedUserIdsRepository.save(new RecommendedUserIds(condition, playgroundUserIds));
        return playgroundUserIds;
    }
}
