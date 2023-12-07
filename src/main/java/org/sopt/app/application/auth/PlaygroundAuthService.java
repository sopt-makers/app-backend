package org.sopt.app.application.auth;

import io.jsonwebtoken.ExpiredJwtException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.app.application.auth.PlaygroundAuthInfo.PlaygroundActivity;
import org.sopt.app.application.auth.PlaygroundAuthInfo.PlaygroundProfileWithId;
import org.sopt.app.common.exception.BadRequestException;
import org.sopt.app.common.exception.UnauthorizedException;
import org.sopt.app.common.response.ErrorCode;
import org.sopt.app.domain.enums.UserStatus;
import org.sopt.app.interfaces.external.PlaygroundClient;
import org.sopt.app.presentation.auth.AppAuthRequest;
import org.sopt.app.presentation.user.UserRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException.BadRequest;

@Service
@RequiredArgsConstructor
public class PlaygroundAuthService {

    private final PlaygroundClient playgroundClient;
    @Value("${sopt.current.generation}")
    private Long currentGeneration;
    @Value("${makers.playground.x-api-key}")
    private String apiKey;
    @Value("${makers.playground.x-request-from}")
    private String requestFrom;

    public PlaygroundAuthInfo.PlaygroundMain getPlaygroundInfo(String token) {
        val member = this.getPlaygroundMember(token);
        val playgroundProfile = this.getPlaygroundMemberProfile(token);
        val generationList = playgroundProfile.getActivities().stream()
                .map(activity -> activity.getCardinalActivities().get(0).getGeneration()).collect(Collectors.toList());
        member.setAccessToken(token);
        member.setStatus(this.getStatus(generationList));
        return member;
    }

    public AppAuthRequest.AccessTokenRequest getPlaygroundAccessToken(AppAuthRequest.CodeRequest codeRequest) {
        Map<String, String> headers = createDefaultHeader();
        try {
            return playgroundClient.getAccessToken(headers, codeRequest);
        } catch (Exception e) {
            throw new BadRequestException(ErrorCode.INVALID_PLAYGROUND_CODE.getMessage());
        }
    }

    private PlaygroundAuthInfo.PlaygroundMain getPlaygroundMember(String accessToken) {
        Map<String, String> headers = createDefaultHeader();
        headers.put("Authorization", accessToken);
        try {
            return playgroundClient.getPlaygroundMember(headers);
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(ErrorCode.INVALID_PLAYGROUND_TOKEN.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(ErrorCode.PLAYGROUND_USER_NOT_EXISTS.getMessage());
        }
    }

    public PlaygroundAuthInfo.RefreshedToken refreshPlaygroundToken(AppAuthRequest.AccessTokenRequest tokenRequest) {
        Map<String, String> headers = createDefaultHeader();
        headers.put("x-api-key", apiKey);
        headers.put("x-request-from", requestFrom);
        try {
            return playgroundClient.refreshPlaygroundToken(headers, tokenRequest);
        } catch (BadRequest badRequest) {
            throw new UnauthorizedException(ErrorCode.INVALID_PLAYGROUND_TOKEN.getMessage());
        } catch (ExpiredJwtException e) {
        throw new UnauthorizedException(ErrorCode.INVALID_PLAYGROUND_TOKEN.getMessage());
    }
    }

    public PlaygroundAuthInfo.MainView getPlaygroundUserForMainView(String accessToken) {
        val playgroundProfile = this.getPlaygroundMemberProfile(accessToken);
        val profileImage = playgroundProfile.getProfileImage() == null ? "" : playgroundProfile.getProfileImage();
        val generationList = playgroundProfile.getActivities().stream()
                .map(activity -> activity.getCardinalActivities().get(0).getGeneration()).collect(Collectors.toList());
        Collections.sort(generationList, Collections.reverseOrder());
        val mainViewUser = PlaygroundAuthInfo.MainViewUser.builder()
                .status(this.getStatus(generationList))
                .name(playgroundProfile.getName())
                .profileImage(profileImage)
                .generationList(generationList)
                .build();
        return PlaygroundAuthInfo.MainView.builder().user(mainViewUser).build();
    }

    private UserStatus getStatus(List<Long> generationList) {
        return generationList.contains(currentGeneration) ? UserStatus.ACTIVE : UserStatus.INACTIVE;
    }

    private PlaygroundAuthInfo.PlaygroundProfile getPlaygroundMemberProfile(String accessToken) {
        Map<String, String> headers = createDefaultHeader();
        headers.put("Authorization", accessToken);
        try {
            return playgroundClient.getPlaygroundMemberProfile(headers);
        } catch (BadRequest e) {
            throw new BadRequestException(ErrorCode.PLAYGROUND_PROFILE_NOT_EXISTS.getMessage());
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(ErrorCode.INVALID_PLAYGROUND_TOKEN.getMessage());
        }
    }

    public PlaygroundAuthInfo.UserActiveInfo getPlaygroundUserActiveInfo(String accessToken) {
        val playgroundProfile = this.getPlaygroundMemberProfile(accessToken);
        val generationList = playgroundProfile.getActivities().stream()
            .map(activity -> activity.getCardinalActivities().get(0).getGeneration()).toList();
        val userStatus = this.getStatus(generationList);
        return PlaygroundAuthInfo.UserActiveInfo.builder()
                .status(userStatus)
                .currentGeneration(currentGeneration)
                .build();
    }

    // Header 생성 메서드
    private Map<String, String> createDefaultHeader() {
        return new HashMap<>(Map.of("content-type", "application/json;charset=UTF-8"));
    }

    public PlaygroundAuthInfo.ActiveUserIds getPlayGroundUserIds(String accessToken) {
        Map<String, String> headers = createDefaultHeader();
        headers.put("Authorization", accessToken);
        try {
            return playgroundClient.getPlaygroundUserIds(headers, currentGeneration);
        } catch (BadRequest e) {
            throw new BadRequestException(ErrorCode.PLAYGROUND_PROFILE_NOT_EXISTS.getMessage());
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(ErrorCode.INVALID_PLAYGROUND_TOKEN.getMessage());
        }
    }

    public List<PlaygroundAuthInfo.MemberProfile> getPlaygroundMemberProfiles(String accessToken, List<Long> memberIds) {
        Map<String, String> defaultHeader = createDefaultHeader();
        defaultHeader.put("Authorization", accessToken);
        UserRequest.MemberProfilesRequest request = new UserRequest.MemberProfilesRequest();
        request.setMemberIds(memberIds);
        try {
            return playgroundClient.getMemberProfiles(defaultHeader, request);
            // TODO : 임시 Mock Data
            //return List.of(new PlaygroundAuthInfo.MemberProfile(122L, null, "구건모", List.of(new PlaygroundAuthInfo.ActivityCardinalInfo("32,웹"))));
        } catch (BadRequest e) {
            throw new BadRequestException(ErrorCode.PLAYGROUND_PROFILE_NOT_EXISTS.getMessage());
        } catch (ExpiredJwtException e) {
        throw new UnauthorizedException(ErrorCode.INVALID_PLAYGROUND_TOKEN.getMessage());
    }
    }

    public List<PlaygroundAuthInfo.PlaygroundProfileWithId> getPlaygroundProfiles(List<Long> recommendUserIds) {
        val result = recommendUserIds.stream().map(
                userId -> {
                    val dummyActivity = new PlaygroundActivity();
                    val dummyCardinalActivity = new PlaygroundAuthInfo.PlaygroundCardinalActivity();
                    dummyCardinalActivity.setGeneration(currentGeneration);
                    dummyCardinalActivity.setId(1L);
                    dummyCardinalActivity.setIsProject(false);
                    dummyCardinalActivity.setPart("서버");
                    dummyCardinalActivity.setTeam("없음");
                    dummyActivity.setCardinalActivities(List.of(dummyCardinalActivity));

                    val playgroundProfile = new PlaygroundProfileWithId(userId);
                    playgroundProfile.setProfileImage("");
                    playgroundProfile.setName("test");
                    playgroundProfile.setActivities(List.of(dummyActivity));
                    return playgroundProfile;
                }
        ).toList();
        return result;
    }
}
