package org.sopt.app.presentation.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

import lombok.*;
import org.sopt.app.domain.enums.NotificationCategory;
import org.sopt.app.domain.enums.NotificationType;

import java.util.List;

public class NotificationRequest {

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class RegisterNotificationRequest {
        @Schema(description = "알림 서버로부터 수신한 알림의 고유 ID", example = "1")
        @NotNull(message = "message id may not be null")
        private Long messageId;

        @Schema(description = "알림 대상 유저 플레이그라운드 ID 리스트", example = "['1', '2']")
        @JsonProperty(value = "userIds")
        private List<String> userIds;

        @Schema(description = "알림 제목", example = "앱팀 최고")
        @NotNull(message = "title may not be null")
        private String title;

        @Schema(description = "알림 제목", example = "앱팀 최고")
        private String content;

        @Schema(description = "알림 범위 타입", example = "INDIVIDUAL")
        @NotNull(message = "type may not be null")
        private NotificationType type;

        @Schema(description = "알림 카테고리", example = "NOTICE")
        @NotNull(message = "category may not be null")
        private NotificationCategory category;

        @Schema(description = "알림 첨부 딥링크")
        @JsonProperty(value = "deepLink")
        private String deepLink;

        @Schema(description = "알림 첨부 웹링크")
        @JsonProperty(value = "webLink")
        private String webLink;

    }

}
