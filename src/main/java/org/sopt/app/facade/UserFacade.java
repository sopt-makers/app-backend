package org.sopt.app.facade;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.app.application.auth.PlaygroundAuthService;
import org.sopt.app.application.notification.NotificationService;
import org.sopt.app.application.operation.OperationInfo;
import org.sopt.app.domain.entity.User;
import org.sopt.app.presentation.user.UserResponse.MainView;
import org.sopt.app.presentation.user.UserResponseMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserFacade {

    private final PlaygroundAuthService playgroundAuthService;
    private final NotificationService notificationService;
    private final UserResponseMapper userResponseMapper;

    @Transactional(readOnly = true)
    public MainView getMainViewInfo(User user, String accessToken) {
        val mainViewUser = playgroundAuthService.getPlaygroundUserForMainView(user.getPlaygroundToken());
        val dummyOperation = OperationInfo.MainView.builder().announcement("공지다!").attendanceScore(2D).build();
        val mainViewNotification = notificationService.getNotificationConfirmStatus(user);
        return userResponseMapper.ofMainView(mainViewUser, dummyOperation, mainViewNotification);
    }
}
