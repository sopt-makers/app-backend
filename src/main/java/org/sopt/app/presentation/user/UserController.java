package org.sopt.app.presentation.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.app.application.user.UserService;
import org.sopt.app.domain.entity.User;
import org.sopt.app.presentation.rank.RankRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/user")
@SecurityRequirement(name = "Authorization")
public class UserController {

    private final UserService userService;
    private final UserResponseMapper userResponseMapper;


    @Operation(summary = "유저 정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "500", description = "server error", content = @Content)
    })
    @GetMapping(value = "")
    public ResponseEntity<UserResponse.AppUser> getUserInfo(@AuthenticationPrincipal User user) {
        val response = userResponseMapper.ofAppUser(user);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "솝탬프 정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "500", description = "server error", content = @Content)
    })
    @GetMapping(value = "/soptamp")
    public ResponseEntity<UserResponse.Soptamp> getSoptampInfo(@AuthenticationPrincipal User user) {
        val response = userResponseMapper.ofSoptamp(user);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "닉네임 중복 검사")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "success", content = @Content),
            @ApiResponse(responseCode = "400", description = "duplicate nickname", content = @Content),
            @ApiResponse(responseCode = "500", description = "server error", content = @Content)
    })
    @GetMapping(value = "/nickname/{nickname}")
    public ResponseEntity<UserResponse.Nickname> validateUserNickname(@PathVariable String nickname) {
        userService.checkUserNickname(nickname);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @Operation(summary = "닉네임 변경")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "500", description = "server error", content = @Content)
    })
    @PatchMapping(value = "/nickname")
    public ResponseEntity<UserResponse.Nickname> editNickname(
            @AuthenticationPrincipal User user,
            @RequestBody UserRequest.EditNicknameRequest editNicknameRequest
    ) {
        val nickname = editNicknameRequest.getNickname();
        val result = userService.editNickname(user, nickname);
        val response = userResponseMapper.of(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "한마디 변경")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "500", description = "server error", content = @Content)
    })
    @PatchMapping("/profile-message")
    public ResponseEntity<UserResponse.ProfileMessage> editProfileMessage(
            @AuthenticationPrincipal User user,
            @RequestBody RankRequest.EditProfileMessageRequest editProfileMessageRequest
    ) {
        val result = userService.editProfileMessage(user, editProfileMessageRequest.getProfileMessage());
        val response = userResponseMapper.of(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
