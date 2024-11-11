package org.sopt.app.application.app_service.dto;

import java.util.List;
import lombok.*;
import org.sopt.app.application.app_service.AppServiceName;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AppServiceEntryStatusResponse {

    private final AppServiceName serviceName;
    private final boolean displayAlarmBadge;
    private final String alarmBadge;
    private final boolean displayMessage;
    private final List<String> messages;
    private final List<String> messageColors;

    public static AppServiceEntryStatusResponse createAppServiceEntryStatus (
            AppServiceInfo appServiceInfo, AppServiceBadgeInfo badgeInfo
    ) {
        if (badgeInfo.getDisplayMessage()){
            return createAppServiceEntryStatusByDisPlayMessage(appServiceInfo);
        }
        if(badgeInfo.getDisplayAlarmBadge()){
            return createAppServiceEntryStatusByDisPlayAlarmBadge(appServiceInfo, badgeInfo.getAlarmBadge());
        }
        return createDefaultAppServiceEntryStatus(appServiceInfo);
    }

    private static AppServiceEntryStatusResponse createAppServiceEntryStatusByDisPlayMessage(
            AppServiceInfo appServiceInfo
    ) {
        return AppServiceEntryStatusResponse.builder()
                .serviceName(appServiceInfo.getServiceName())
                .displayAlarmBadge(false)
                .alarmBadge("")
                .displayMessage(true)
                .messages(appServiceInfo.getMessages())
                .messageColors(appServiceInfo.getMessageColors())
                .build();
    }

    private static AppServiceEntryStatusResponse createAppServiceEntryStatusByDisPlayAlarmBadge(
            AppServiceInfo appServiceInfo, String alarmBadge
    ) {
        return AppServiceEntryStatusResponse.builder()
                .serviceName(appServiceInfo.getServiceName())
                .displayAlarmBadge(true)
                .alarmBadge(alarmBadge)
                .displayMessage(false)
                .messages(List.of())
                .messageColors(List.of())
                .build();
    }

    private static AppServiceEntryStatusResponse createDefaultAppServiceEntryStatus(
            AppServiceInfo appServiceInfo
    ) {
        return AppServiceEntryStatusResponse.builder()
                .serviceName(appServiceInfo.getServiceName())
                .displayAlarmBadge(false)
                .alarmBadge("")
                .displayMessage(false)
                .messages(List.of())
                .messageColors(List.of())
                .build();
    }
}
