package org.sopt.app.application.app_service;

import lombok.RequiredArgsConstructor;
import org.sopt.app.application.app_service.dto.AppServiceBadgeInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Qualifier("defaultBadgeManager")
public class DefaultBadgeManager implements AppServiceBadgeManager {
    @Override
    public AppServiceBadgeInfo acquireAppServiceBadgeInfo(final Long userId) {
        return AppServiceBadgeInfo.createWithAllDisabled();
    }
}
