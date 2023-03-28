package org.sopt.app.presentation.stamp;

import static org.sopt.app.common.ResponseCode.DUPLICATE_STAMP;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.val;
import org.sopt.app.application.stamp.StampService;
import org.sopt.app.common.exception.ApiException;
import org.sopt.app.common.s3.S3Service;
import org.sopt.app.domain.entity.User;
import org.sopt.app.presentation.BaseController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v2/stamp")
public class StampController extends BaseController {

    private final StampService stampService;

    private final S3Service s3Service;

    private final StampResponseMapper stampResponseMapper;

    @Operation(summary = "스탬프 조회하기")
    @GetMapping("/{missionId}")
    public ResponseEntity<StampResponse.Main> findStampByMissionAndUserId(
            @AuthenticationPrincipal User user,
            @PathVariable Long missionId
    ) {
        val result = stampService.findStamp(user.getId(), missionId);
        val response = stampResponseMapper.of(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "스탬프 등록하기")
    @PostMapping("/{missionId}")
    public ResponseEntity<StampResponse.Main> registerStamp(
            @AuthenticationPrincipal User user,
            @PathVariable Long missionId,
            @RequestPart("stampContent") StampRequest.RegisterStampRequest registerStampRequest,
            @RequestPart(name = "imgUrl", required = false) List<MultipartFile> multipartFiles
    ) {
        val isDuplicateStamp = stampService.checkDuplicateStamp(user.getId(), missionId);
        if (isDuplicateStamp) {
            throw new ApiException(DUPLICATE_STAMP);
        }
        val imgPaths = s3Service.upload(multipartFiles);
        val result = stampService.uploadStamp(registerStampRequest, imgPaths, user.getId(), missionId);
        val response = stampResponseMapper.of(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "스탬프 수정하기")
    @PutMapping("/{missionId}")
    public ResponseEntity<StampResponse.Id> editStamp(
            @AuthenticationPrincipal User user,
            @PathVariable Long missionId,
            @RequestPart(value = "stampContent", required = false) StampRequest.EditStampRequest editStampRequest,
            @RequestPart(name = "imgUrl", required = false) List<MultipartFile> multipartFiles
    ) {
        val stamp = stampService.editStampContents(editStampRequest, user.getId(), missionId);
        val imgPaths = s3Service.upload(multipartFiles);
        if (imgPaths.size() > 0) {
            stampService.editStampImages(stamp, imgPaths);
        }
        val response = stampResponseMapper.of(stamp.getId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "스탬프 삭제하기(개별)")
    @DeleteMapping("/{stampId}")
    public ResponseEntity<String> deleteStampById(@PathVariable Long stampId) {
        stampService.deleteByStampId(stampId);
        return ResponseEntity.status(HttpStatus.OK).body("{}");
    }

    @Operation(summary = "스탬프 삭제하기(전체)")
    @DeleteMapping("/all")
    public ResponseEntity<String> deleteStampByUserId(@AuthenticationPrincipal User user) {
        stampService.deleteStampByUserId(user.getId());
        return ResponseEntity.status(HttpStatus.OK).body("{}");
    }
}
