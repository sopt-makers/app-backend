package org.sopt.app.presentation.poke;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.app.application.user.UserInfo;
import org.sopt.app.application.user.UserInfo.PorkProfile;
import org.sopt.app.domain.entity.User;
import org.sopt.app.facade.PokeFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/poke")
@RequiredArgsConstructor
public class PokeController {

    private final PokeFacade pokeFacade;

    @GetMapping("/random-user")
    public ResponseEntity<List<PokeResponse.PokeProfile>> getRandomUserForNew(
        @AuthenticationPrincipal User user
    ) {
        val result = pokeFacade.getRecommendUserForNew(
            user.getPlaygroundToken(),
            user.getPlaygroundId()
        );
        val response = result.stream().map(
            profile -> PokeResponse.PokeProfile.of(
                profile.getUserId(),
                profile.getProfileImage(),
                profile.getName(),
                profile.getGeneration(),
                profile.getPart()
            )
        ).toList();
        return ResponseEntity.ok(response);
    }
}
