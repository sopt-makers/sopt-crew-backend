package org.sopt.makers.crew.main.entity.meeting;

import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.entity.meeting.enums.EnMeetingStatus;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;

public class MeetingTest {

    @Test
    void 스터디장인지_확인(){
        // given
        User host = User.builder().name("김철수")
                .orgId(1)
                .activities(List.of(new UserActivityVO("웹", 31)))
                .profileImage(null)
                .phone("010-2222-2222")
                .build();

        Meeting meeting = Meeting.builder()
                .user(host)
                .userId(1)
                .title("모임 제목입니다")
                .category(MeetingCategory.EVENT)
                .startDate(LocalDateTime.of(2024,4,24,0,0,0))
                .endDate(LocalDateTime.of(2024,4,29,23,59,59))
                .capacity(20)
                .desc("모임 소개입니다")
                .processDesc("진행방식 소개입니다")
                .mStartDate(LocalDateTime.of(2024,5,24,0,0,0))
                .mEndDate(LocalDateTime.of(2024,6,24,0,0,0))
                .leaderDesc("스장 소개입니다")
                .targetDesc("모집 대상입니다")
                .isMentorNeeded(true)
                .canJoinOnlyActiveGeneration(false)
                .createdGeneration(34)
                .targetActiveGeneration(null)
                .joinableParts(MeetingJoinablePart.values())
                .build();

        // when
        Boolean isHost1 = meeting.isHost(1);
        Boolean isHost2 = meeting.isHost(2);

        // then
        Assertions.assertThat(isHost1).isEqualTo(true);
        Assertions.assertThat(isHost2).isEqualTo(false);
    }

    @Test
    void 신청기간_이전에_모임_모집상태_확인(){
        // given
        User host = User.builder().name("김철수")
                .orgId(1)
                .activities(List.of(new UserActivityVO("웹", 31)))
                .profileImage(null)
                .phone("010-2222-2222")
                .build();

        Meeting meeting = Meeting.builder()
                .user(host)
                .userId(1)
                .title("모임 제목입니다")
                .category(MeetingCategory.EVENT)
                .startDate(LocalDateTime.of(2024,4,24,0,0,0))
                .endDate(LocalDateTime.of(2024,4,29,23,59,59))
                .capacity(20)
                .desc("모임 소개입니다")
                .processDesc("진행방식 소개입니다")
                .mStartDate(LocalDateTime.of(2024,5,24,0,0,0))
                .mEndDate(LocalDateTime.of(2024,6,24,0,0,0))
                .leaderDesc("스장 소개입니다")
                .targetDesc("모집 대상입니다")
                .isMentorNeeded(true)
                .canJoinOnlyActiveGeneration(false)
                .createdGeneration(34)
                .targetActiveGeneration(null)
                .joinableParts(MeetingJoinablePart.values())
                .build();

        // when
        EnMeetingStatus beforeRecruitment = meeting.getMeetingStatus(LocalDateTime.of(2024, 4, 23, 23, 59, 59));
        EnMeetingStatus recruiting = meeting.getMeetingStatus(LocalDateTime.of(2024, 4, 24, 0, 0, 0));
        EnMeetingStatus closeRecruitment = meeting.getMeetingStatus(LocalDateTime.of(2024, 4, 30, 0, 0, 0));
        EnMeetingStatus active = meeting.getMeetingStatus(LocalDateTime.of(2024, 5, 24, 0, 0, 0));
        EnMeetingStatus activityEnd = meeting.getMeetingStatus(LocalDateTime.of(2024, 6, 24, 0, 0, 1));


        // then
        Assertions.assertThat(beforeRecruitment).isEqualTo(EnMeetingStatus.BEFORE_RECRUITMENT);
        Assertions.assertThat(recruiting).isEqualTo(EnMeetingStatus.RECRUITING);
        Assertions.assertThat(closeRecruitment).isEqualTo(EnMeetingStatus.CLOSE_RECRUITMENT);
        Assertions.assertThat(active).isEqualTo(EnMeetingStatus.ACTIVE);
        Assertions.assertThat(activityEnd).isEqualTo(EnMeetingStatus.ACTIVITY_END);

    }

}
