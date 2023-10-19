package org.sopt.app.application.stamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.app.common.event.Events;
import org.sopt.app.common.exception.BadRequestException;
import org.sopt.app.common.response.ErrorCode;
import org.sopt.app.domain.entity.Stamp;
import org.sopt.app.interfaces.postgres.StampRepository;
import org.sopt.app.presentation.stamp.StampRequest;
import org.sopt.app.presentation.stamp.StampRequest.RegisterStampRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class StampService {

    private final StampRepository stampRepository;

    @Transactional(readOnly = true)
    public StampInfo.Stamp findStamp(Long missionId, Long userId) {
        val entity = stampRepository.findByUserIdAndMissionId(userId, missionId)
            .orElseThrow(() -> new BadRequestException(ErrorCode.STAMP_NOT_FOUND.getMessage()));
        return StampInfo.Stamp.builder()
            .id(entity.getId())
            .contents(entity.getContents())
            .images(entity.getImages())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    @Transactional
    public StampInfo.Stamp uploadStampDeprecated(
            RegisterStampRequest stampRequest,
            List<String> imgPaths,
            Long userId,
            Long missionId) {
        val imgList = new ArrayList<>(imgPaths);
        val stamp = this.convertStampImgDeprecated(stampRequest, imgList, userId, missionId);
        val newStamp = stampRepository.save(stamp);
        return StampInfo.Stamp.builder()
                .id(newStamp.getId())
                .contents(newStamp.getContents())
                .images(newStamp.getImages())
                .createdAt(newStamp.getCreatedAt())
                .updatedAt(newStamp.getUpdatedAt())
                .build();
    }

    @Transactional
    public StampInfo.Stamp uploadStamp(
            RegisterStampRequest stampRequest,
            Long userId) {
        val stamp = Stamp.builder()
                .contents(stampRequest.getContents())
                .createdAt(LocalDateTime.now())
                .images(List.of(stampRequest.getImage()))
                .missionId(stampRequest.getMissionId())
                .userId(userId)
                .build();

        val newStamp = stampRepository.save(stamp);
        return StampInfo.Stamp.builder()
                .id(newStamp.getId())
                .contents(newStamp.getContents())
                .images(newStamp.getImages())
                .createdAt(newStamp.getCreatedAt())
                .updatedAt(newStamp.getUpdatedAt())
                .build();
    }

    @Transactional
    public StampInfo.Stamp editStampContentsDeprecated(
            StampRequest.EditStampRequest editStampRequest,
            Long userId,
            Long missionId) {

        val stamp = stampRepository.findByUserIdAndMissionId(userId, missionId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.STAMP_NOT_FOUND.getMessage()));
        if (StringUtils.hasText(editStampRequest.getContents())) {
            stamp.changeContents(editStampRequest.getContents());
        }

        stamp.setUpdatedAt(LocalDateTime.now());
        val newStamp = stampRepository.save(stamp);
        return StampInfo.Stamp.builder()
                .id(newStamp.getId())
                .contents(newStamp.getContents())
                .images(newStamp.getImages())
                .createdAt(newStamp.getCreatedAt())
                .updatedAt(newStamp.getUpdatedAt())
                .build();
    }

    @Transactional
    public StampInfo.Stamp editStampContents(
            StampRequest.EditStampRequest editStampRequest,
            Long userId) {

        val stamp = stampRepository.findByUserIdAndMissionId(userId, editStampRequest.getMissionId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.STAMP_NOT_FOUND.getMessage()));
        if (StringUtils.hasText(editStampRequest.getContents())) {
            stamp.changeContents(editStampRequest.getContents());
        }
        if (StringUtils.hasText(editStampRequest.getImage())) {
            stamp.changeImages(List.of(editStampRequest.getImage()));
        }
        stamp.setUpdatedAt(LocalDateTime.now());
        val newStamp = stampRepository.save(stamp);
        return StampInfo.Stamp.builder()
                .id(newStamp.getId())
                .contents(newStamp.getContents())
                .images(newStamp.getImages())
                .createdAt(newStamp.getCreatedAt())
                .updatedAt(newStamp.getUpdatedAt())
                .build();
    }

    @Transactional
    public StampInfo.Stamp editStampImagesDeprecated(StampInfo.Stamp stamp, List<String> imgPaths) {
        val oldStamp = stampRepository.findById(stamp.getId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.STAMP_NOT_FOUND.getMessage()));
        oldStamp.changeImages(imgPaths);
        val newStamp = stampRepository.save(oldStamp);
        return StampInfo.Stamp.builder()
                .id(newStamp.getId())
                .contents(newStamp.getContents())
                .images(newStamp.getImages())
                .createdAt(newStamp.getCreatedAt())
                .updatedAt(newStamp.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public void checkDuplicateStamp(Long userId, Long missionId) {
        if (stampRepository.findByUserIdAndMissionId(userId, missionId).isPresent()) {
            throw new BadRequestException(ErrorCode.DUPLICATE_STAMP.getMessage());
        }
    }

    @Transactional
    public void deleteStampById(Long stampId) {

        val stamp = stampRepository.findById(stampId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.STAMP_NOT_FOUND.getMessage()));
        stampRepository.deleteById(stampId);

        Events.raise(new StampDeletedEvent(stamp.getImages()));
    }

    @Transactional
    public void deleteAllStamps(Long userId) {
        stampRepository.deleteAllByUserId(userId);

        val imageUrls = stampRepository.findAllByUserId(userId).stream().map(Stamp::getImages)
                .flatMap(images -> images.stream()).collect(Collectors.toList());
        Events.raise(new StampDeletedEvent(imageUrls));
    }


    private Stamp convertStampImgDeprecated(
            RegisterStampRequest stampRequest,
            List<String> imgList,
            Long userId,
            Long missionId) {
        return Stamp.builder()
                .contents(stampRequest.getContents())
                .createdAt(LocalDateTime.now())
                .images(imgList)
                .missionId(missionId)
                .userId(userId)
                .build();
    }

    public Long getMissionIdByStampId(Long stampId) {
        return stampRepository.findById(stampId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.STAMP_NOT_FOUND.getMessage()))
                .getMissionId();
    }
}
