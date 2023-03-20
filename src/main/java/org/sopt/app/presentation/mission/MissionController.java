package org.sopt.app.presentation.mission;


import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.val;
import org.sopt.app.application.mission.MissionService;
import org.sopt.app.common.s3.S3Service;
import org.sopt.app.presentation.BaseController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/mission")
public class MissionController extends BaseController {

    private final MissionService missionService;
    private final S3Service s3Service;
    private final MissionResponseMapper missionResponseMapper;


    @Operation(summary = "미션 전체 조회하기")
    @GetMapping(value = "/all")
    @ResponseBody
    public ResponseEntity<List<MissionResponse.Completeness>> findAllMission(@RequestHeader("userId") String userId) {
        val result = missionService.findAllMission(userId);
        val response = missionResponseMapper.ofCompleteness(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @Operation(summary = "미션 생성하기")
    @PostMapping()
    public ResponseEntity<MissionResponse.Id> registerMission(
            @RequestPart("missionContent") MissionRequest.RegisterMissionRequest registerMissionRequest,
            @RequestPart(name = "imgUrl", required = false) List<MultipartFile> multipartFiles) {
        val mission = missionService.uploadMission(registerMissionRequest);
        val imgPaths = s3Service.upload(multipartFiles);
        if (imgPaths.size() > 0) {
            missionService.editMissionWithImages(mission, imgPaths);
        }
        val response = missionResponseMapper.of(mission.getId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "완료 미션만 조회하기")
    @GetMapping("complete")
    public ResponseEntity<List<MissionResponse.Main>> findCompleteMission(@RequestHeader("userId") String userId) {
        val result = missionService.getCompleteMission(userId);
        val response = missionResponseMapper.of(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "미완료 미션만 조회하기")
    @GetMapping("incomplete")
    public ResponseEntity<List<MissionResponse.Main>> findInCompleteMission(@RequestHeader("userId") String userId) {
        val result = missionService.getIncompleteMission(userId);
        val response = missionResponseMapper.of(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
