package org.sopt.app.presentation.description;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.app.facade.DescriptionFacade;
import org.sopt.app.domain.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/description")
@SecurityRequirement(name = "Authorization")
public class DescriptionController {

    private final DescriptionFacade descriptionFacade;

    @Operation(summary = "메인 문구 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "success"),
        @ApiResponse(responseCode = "401", description = "token error", content = @Content),
        @ApiResponse(responseCode = "500", description = "server error", content = @Content)
    })
    @GetMapping("/main")
    public ResponseEntity<DescriptionResponse.MainDescription> getMainDescription(
        @AuthenticationPrincipal User user
    ) {
        val response = descriptionFacade.getMainDescriptionForUser(user);
        return ResponseEntity.ok(
            DescriptionResponse.MainDescription.builder()
                .topDescription(response.getTopDescription())
                .bottomDescription(response.getBottomDescription())
                .build()
        );
    }
}
