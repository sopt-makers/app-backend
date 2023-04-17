package org.sopt.app.application.mission;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.app.domain.entity.Mission;
import org.sopt.app.domain.entity.Stamp;
import org.sopt.app.interfaces.postgres.MissionRepository;
import org.sopt.app.interfaces.postgres.StampRepository;
import org.sopt.app.presentation.mission.MissionRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final StampRepository stampRepository;

    @Transactional(readOnly = true)
    public List<MissionInfo.Completeness> findAllMission(Long userId) {
        val completedStampList = stampRepository.findAllByUserId(userId);
        val missionList = missionRepository.findAll();
        return missionList.stream()
                .map(mission -> MissionInfo.Completeness.builder()
                        .id(mission.getId())
                        .title(mission.getTitle())
                        .level(mission.getLevel())
                        .profileImage(mission.getProfileImage())
                        .isCompleted(isCompletedMission(mission.getId(), completedStampList))
                        .build())
                .sorted(Comparator.comparing(MissionInfo.Completeness::getLevel)
                        .thenComparing(MissionInfo.Completeness::getTitle))
                .collect(Collectors.toList());
    }

    private Boolean isCompletedMission(Long missionId, List<Stamp> completedStamps) {
        return completedStamps.stream().anyMatch(
                stamp -> stamp.getMissionId().equals(missionId));
    }

    // 게시글 작성 - 이미지 미포함
    @Transactional
    public Mission uploadMission(MissionRequest.RegisterMissionRequest registerMissionRequest) {
        val mission = Mission.builder()
                .title(registerMissionRequest.getTitle())
                .level(registerMissionRequest.getLevel())
                .display(true)
                .profileImage(List.of(registerMissionRequest.getImage()))
                .build();
        return missionRepository.save(mission);
    }

    //Mission 완료한 미션만 불러오기
    @Transactional(readOnly = true)
    public List<Mission> getCompleteMission(Long userId) {
        val stampList = stampRepository.findAllByUserId(userId);
        val missionIdList = stampList.stream().map(Stamp::getMissionId).collect(Collectors.toList());
        return missionRepository.findMissionInOrderByLevelAndTitle(missionIdList);
    }

    @Transactional(readOnly = true)
    public List<Mission> getIncompleteMission(Long userId) {

        //전체 미션 조회하기
        val missionList = missionRepository.findAll();
        val missionIdList = missionList.stream().map(Mission::getId).collect(Collectors.toList());

        //stamp에서 userId로 달성한 mission 조회하기
        val stampList = stampRepository.findAllByUserId(userId);
        val completeMissionIdList = stampList.stream().map(Stamp::getMissionId).collect(Collectors.toList());

        //두 리스트 비교해서 중복값 제거
        val inCompleteIdList = missionIdList.stream()
                .filter(all -> completeMissionIdList.stream().noneMatch(Predicate.isEqual(all)))
                .toList();

        return missionRepository.findMissionInOrderByLevelAndTitle(inCompleteIdList);
    }
}

