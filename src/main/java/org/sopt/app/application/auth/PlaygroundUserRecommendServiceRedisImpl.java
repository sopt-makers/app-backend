package org.sopt.app.application.auth;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.sopt.app.application.auth.PlaygroundAuthInfo.PlaygroundUserIds;
import org.sopt.app.application.auth.PlaygroundAuthInfo.RecommendFriendRequest;
import org.sopt.app.domain.entity.RecommendedUsers;
import org.sopt.app.interfaces.external.PlaygroundClient;
import org.sopt.app.interfaces.postgres.RecommendedUserIdsRepository;

@RequiredArgsConstructor
public class PlaygroundUserRecommendServiceRedisImpl implements PlaygroundUserRecommendService {

    private final PlaygroundClient playgroundClient;
    private final RecommendedUserIdsRepository recommendedUserIdsRepository;

    @Override
    public List<Long> getPlaygroundUserIdsForSameRecommendType(
            final Map<String, String> authHeader, final RecommendFriendRequest request) {
        Optional<RecommendedUsers> recommendedUsers = recommendedUserIdsRepository.findById(request);

        if (recommendedUsers.isPresent()) {
            return recommendedUsers.get().getPlaygroundUserIds().getUserIds();
        }

        PlaygroundUserIds playgroundUserIds = playgroundClient.getPlaygroundUserIdsForSameRecommendType(authHeader, request);
        return recommendedUserIdsRepository.save(new RecommendedUsers(request, playgroundUserIds))
                .getPlaygroundUserIds()
                .getUserIds();
    }
}
