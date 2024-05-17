package org.sopt.makers.crew.main.common.util;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.user.enums.UserPart;

@RequiredArgsConstructor
public class UserPartUtil {

    public static MeetingJoinablePart getMeetingJoinablePart(UserPart userPart) {
        switch (userPart) {
            case PM:
            case PM_LEADER:
                return MeetingJoinablePart.PM;
            case DESIGN:
            case DESIGN_LEADER:
                return MeetingJoinablePart.DESIGN;
            case IOS:
            case IOS_LEADER:
                return MeetingJoinablePart.IOS;
            case ANDROID:
            case ANDROID_LEADER:
                return MeetingJoinablePart.ANDROID;
            case SERVER:
            case SERVER_LEADER:
                return MeetingJoinablePart.SERVER;
            case WEB:
            case WEB_LEADER:
                return MeetingJoinablePart.WEB;
            default:
                // 임원진 등
                return null;
        }
    }
}
