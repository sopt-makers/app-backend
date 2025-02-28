package org.sopt.app.common.fixtures;

import java.util.List;
import org.sopt.app.application.soptamp.SoptampUserInfo;
import org.sopt.app.domain.entity.soptamp.SoptampUser;
import org.sopt.app.domain.enums.PlaygroundPart;

public class SoptampUserFixture {

    public static final SoptampUser SOPTAMP_USER_1 =
            SoptampUser.builder().id(1L).userId(10L).nickname("서버유저").totalPoints(100L)
                    .generation(35L).part(PlaygroundPart.SERVER).build();
    public static final SoptampUser SOPTAMP_USER_2 =
            SoptampUser.builder().id(2L).userId(20L).nickname("안드유저").generation(35L).totalPoints(200L)
                    .generation(35L).part(PlaygroundPart.ANDROID).build();
    public static final SoptampUser SOPTAMP_USER_3 =
            SoptampUser.builder().id(3L).userId(30L).nickname("아요유저").generation(35L).totalPoints(300L)
                    .generation(35L).part(PlaygroundPart.IOS).build();
    public static final SoptampUser SOPTAMP_USER_4 =
            SoptampUser.builder().id(4L).userId(40L).nickname("디자인유저").generation(35L).totalPoints(300L)
                    .generation(35L).part(PlaygroundPart.DESIGN).build();
    public static final SoptampUser SOPTAMP_USER_5 =
            SoptampUser.builder().id(5L).userId(50L).nickname("서버유저A").generation(35L).totalPoints(500L)
                    .generation(35L).part(PlaygroundPart.SERVER).build();
    public static final SoptampUser SOPTAMP_USER_6 =
            SoptampUser.builder().id(6L).userId(60L).nickname("서버유저B").generation(35L).totalPoints(600L)
                    .generation(35L).part(PlaygroundPart.SERVER).build();

    public static final SoptampUserInfo SOPTAMP_USER_INFO_1 = SoptampUserInfo.builder()
            .id(SOPTAMP_USER_1.getId())
            .userId(SOPTAMP_USER_1.getUserId())
            .nickname(SOPTAMP_USER_1.getNickname())
            .totalPoints(SOPTAMP_USER_1.getTotalPoints()).build();
    public static final SoptampUserInfo SOPTAMP_USER_INFO_2 = SoptampUserInfo.builder()
            .id(SOPTAMP_USER_2.getId())
            .userId(SOPTAMP_USER_2.getUserId())
            .nickname(SOPTAMP_USER_2.getNickname())
            .totalPoints(SOPTAMP_USER_2.getTotalPoints()).build();
    public static final SoptampUserInfo SOPTAMP_USER_INFO_3 = SoptampUserInfo.builder()
            .id(SOPTAMP_USER_3.getId())
            .userId(SOPTAMP_USER_3.getUserId())
            .nickname(SOPTAMP_USER_3.getNickname())
            .totalPoints(SOPTAMP_USER_3.getTotalPoints()).build();
    public static final SoptampUserInfo SOPTAMP_USER_INFO_4 = SoptampUserInfo.builder().
            id(SOPTAMP_USER_4.getId())
            .userId(SOPTAMP_USER_4.getUserId())
            .nickname(SOPTAMP_USER_4.getNickname())
            .totalPoints(SOPTAMP_USER_4.getTotalPoints()).build();
    public static final SoptampUserInfo SOPTAMP_USER_INFO_5 = SoptampUserInfo.builder()
            .id(SOPTAMP_USER_5.getId())
            .userId(SOPTAMP_USER_5.getUserId())
            .nickname(SOPTAMP_USER_5.getNickname())
            .totalPoints(SOPTAMP_USER_5.getTotalPoints()).build();
    public static final SoptampUserInfo SOPTAMP_USER_INFO_6 = SoptampUserInfo.builder()
            .id(SOPTAMP_USER_6.getId())
            .userId(SOPTAMP_USER_6.getUserId())
            .nickname(SOPTAMP_USER_6.getNickname())
            .totalPoints(SOPTAMP_USER_6.getTotalPoints()).build();
    public static final List<SoptampUserInfo> SOPTAMP_USER_INFO_LIST =
            List.of(SOPTAMP_USER_INFO_6, SOPTAMP_USER_INFO_5, SOPTAMP_USER_INFO_4,
                    SOPTAMP_USER_INFO_3, SOPTAMP_USER_INFO_2, SOPTAMP_USER_INFO_1);
    public static final List<SoptampUserInfo> SERVER_PART_SOPTAMP_USER_INFO_LIST =
            List.of(SOPTAMP_USER_INFO_6, SOPTAMP_USER_INFO_5, SOPTAMP_USER_INFO_1);
}
