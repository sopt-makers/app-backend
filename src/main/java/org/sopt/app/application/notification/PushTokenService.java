package org.sopt.app.application.notification;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.app.common.exception.BadRequestException;
import org.sopt.app.domain.entity.PushToken;
import org.sopt.app.domain.entity.User;
import org.sopt.app.domain.enums.PushTokenPlatform;
import org.sopt.app.interfaces.postgres.PushTokenRepository;
import org.sopt.app.presentation.notification.PushTokenRequest;
import org.sopt.app.presentation.notification.PushTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PushTokenService {

    private static final String ACTION_REGISTER = "register";

    private static final String ACTION_DELETE = "cancel";

    private final PushTokenRepository pushTokenRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${makers.push.server}")
    private String baseURI;

    @Value("${makers.push.x-api-key}")
    private String apiKey;


    @Transactional(rollbackFor = BadRequestException.class)
    public PushTokenResponse.StatusResponse registerDeviceToken(User user, String pushToken, String platform) {
        if (!pushTokenRepository.existsByUserIdAndToken(user.getId(), pushToken)) {
            PushToken registerToken = PushToken.builder()
                    .userId(user.getId())
                    .playgroundId(user.getPlaygroundId())
                    .token(pushToken)
                    .platform(PushTokenPlatform.valueOf(platform))
                    .build();
            try {
                val entity = new HttpEntity(
                        createBodyFor(registerToken),
                        createHeadersFor(ACTION_REGISTER, platform)
                );
                val response = sendRequestToPushServer(entity);
                pushTokenRepository.save(registerToken);
                return response.getBody();
            } catch (BadRequestException e) {
                return PushTokenResponse.StatusResponse.builder()
                        .status(e.getStatusCode().value())
                        .success(false)
                        .message(e.getResponseMessage())
                        .build();
            }
        }
        return PushTokenResponse.StatusResponse.builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("토큰 등록 성공")
                .build();
    }


    // 유효하지 않은 토큰으로 인해 BadRequest가 발생하더라도 넘어가야함.(Local 에는 모든 토큰을 쌓아놓기 때문에)
    @Transactional
    public PushTokenResponse.StatusResponse deleteDeviceToken(User user, String pushToken) {
        Optional<PushToken> targetToken = pushTokenRepository.findByUserIdAndToken(user.getId(), pushToken);
        if (targetToken.isPresent()) {
            try {
                val entity = new HttpEntity(
                        createBodyFor(targetToken.get()),
                        createHeadersFor(ACTION_DELETE, targetToken.get().getPlatform().name())
                );
                val response = sendRequestToPushServer(entity);
                pushTokenRepository.delete(targetToken.get());
                return response.getBody();
            } catch (BadRequestException e) {
                return PushTokenResponse.StatusResponse.builder()
                        .status(e.getStatusCode().value())
                        .success(false)
                        .message(e.getResponseMessage())
                        .build();
            }
        }
        return PushTokenResponse.StatusResponse.builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("토큰 삭제 성공")
                .build();
    }

    @Transactional
    public void deleteAllDeviceTokenOf(User user) {
        List<PushToken> userTokens = pushTokenRepository.findAllByUserId(user.getId());
        if (!userTokens.isEmpty()) {
            for (PushToken token : userTokens) {
                deleteDeviceToken(user, token.getToken());
            }
            pushTokenRepository.deleteAll(userTokens);
        }
    }

    private HttpHeaders createHeadersFor(String action, String platform) {
        val headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("action", action);
        headers.add("x-api-key", apiKey);
        headers.add("service", "app");
        headers.add("transactionId", UUID.randomUUID().toString());
        headers.add("platform", platform);
        return headers;
    }

    private PushTokenRequest.PushTokenManageRequest createBodyFor(PushToken pushToken) {
        return PushTokenRequest.PushTokenManageRequest.of(
                List.of(String.valueOf(pushToken.getPlaygroundId())),
                pushToken.getToken()
        );
    }
    private ResponseEntity<PushTokenResponse.StatusResponse> sendRequestToPushServer(HttpEntity requestEntity) {
        return restTemplate.exchange(
                baseURI,
                HttpMethod.POST,
                requestEntity,
                PushTokenResponse.StatusResponse.class
        );
    }
}
