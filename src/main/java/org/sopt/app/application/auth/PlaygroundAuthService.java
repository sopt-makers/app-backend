package org.sopt.app.application.auth;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.app.presentation.auth.AuthRequest;
import org.sopt.app.presentation.auth.AuthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class PlaygroundAuthService {

    @Value("${makers.playground.server.dev}")
    private String baseURI;

    public AuthRequest.AccessTokenRequest getPlaygroundAccessToken(AuthRequest.CodeRequest codeRequest) {
        val getTokenURL = baseURI + "/api/v1/idp/sso/auth";

        val headers = new HttpHeaders();
        headers.add("content-type", "application/json;charset=UTF-8");

        val entity = new HttpEntity(codeRequest, headers);

        val rt = new RestTemplate();
        val response = rt.exchange(
                getTokenURL,
                HttpMethod.POST,
                entity,
                AuthRequest.AccessTokenRequest.class
        );
        return response.getBody();
    }

    public AuthResponse.PlaygroundMemberResponse getPlaygroundMember(String accessToken) {
        val getUserURL = baseURI + "/internal/api/v1/members/me";

        val headers = new HttpHeaders();
        headers.add("content-type", "application/json;charset=UTF-8");
        headers.add("Authorization", accessToken);

        val entity = new HttpEntity(null, headers);

        val rt = new RestTemplate();
        val response = rt.exchange(
                getUserURL,
                HttpMethod.GET,
                entity,
                AuthResponse.PlaygroundMemberResponse.class
        );
        return response.getBody();
    }


}
