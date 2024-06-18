package org.sopt.app.common.fixtures;

import java.util.List;
import org.sopt.app.application.soptamp.SoptampUserInfo;
import org.sopt.app.application.stamp.StampInfo;
import org.sopt.app.presentation.stamp.StampRequest.EditStampRequest;
import org.sopt.app.presentation.stamp.StampRequest.RegisterStampRequest;


public class SoptampFixture {

    public static final Long MISSION_ID = 1L;
    public static final Long USER_ID = 10L;
    public static final Long SOPTAMP_USER_ID = 100L;
    public static final String NICKNAME = "nickname";
    public static final String STAMP_CONTENTS = "stamp contents";
    public static final String STAMP_IMAGE = "stamp image";
    public static final List<String> STAMP_IMG_PATHS = List.of("image");
    public static final String STAMP_ACTIVITY_DATE = "2024.04.08";

    public static SoptampUserInfo.SoptampUser getUserInfo() {
        return SoptampUserInfo.SoptampUser.builder().id(SOPTAMP_USER_ID).userId(USER_ID).nickname(NICKNAME).build();
    }

    public static StampInfo.Stamp getStampInfo() {
        return StampInfo.Stamp.builder().id(SOPTAMP_USER_ID).userId(USER_ID).missionId(MISSION_ID).build();
    }

    public static RegisterStampRequest getRegisterStampRequest() {
        return new RegisterStampRequest(MISSION_ID, STAMP_IMAGE, STAMP_CONTENTS, STAMP_ACTIVITY_DATE);
    }

    public static EditStampRequest getEditStampRequest() {
        return new EditStampRequest(MISSION_ID, STAMP_IMAGE, STAMP_CONTENTS, STAMP_ACTIVITY_DATE);
    }
}
