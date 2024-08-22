package org.sopt.makers.crew.main.meeting.v2.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.sopt.makers.crew.main.common.exception.ErrorStatus.FULL_MEETING_CAPACITY;
import static org.sopt.makers.crew.main.common.exception.ErrorStatus.NOT_IN_APPLY_PERIOD;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.common.exception.BadRequestException;
import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserFixture;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.entity.user.enums.UserPart;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.meeting.v2.dto.ApplyMapper;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingByOrgUserQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2ApplyMeetingDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetAllMeetingByOrgUserDto;

@ExtendWith(MockitoExtension.class)
public class MeetingV2ServiceTest {
    @InjectMocks
    private MeetingV2ServiceImpl meetingV2Service;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplyRepository applyRepository;
    @Mock
    private MeetingRepository meetingRepository;
    @Mock
    private ApplyMapper applyMapper;


    private List<Apply> applies;

    private User ownerUser;

    private User applyUser;

    private Meeting meeting;

    @BeforeEach
    void init() {
        List<UserActivityVO> activityVOS = new ArrayList<>();
        activityVOS.add(new UserActivityVO(UserPart.SERVER.getValue(), 33));

        ownerUser = User.builder()
                .name("송민규")
                .orgId(1)
                .activities(activityVOS)
                .phone("010-9472-6796")
                .build();
        ownerUser.setUserIdForTest(1);

        applyUser = User.builder()
                .name("홍길동")
                .orgId(2)
                .activities(activityVOS)
                .phone("010-1234-5678")
                .build();
        applyUser.setUserIdForTest(2);

        ImageUrlVO imageUrlVO = new ImageUrlVO(1, "www.~~");

        meeting = Meeting.builder()
                .user(ownerUser)
                .userId(ownerUser.getId())
                .title("사람 구해요")
                .category(MeetingCategory.STUDY)
                .imageURL(List.of(imageUrlVO))
                .startDate(LocalDateTime.of(2024, Month.MARCH, 17, 0, 0))
                .endDate(LocalDateTime.of(2024, Month.MARCH, 20, 23, 59))
                .capacity(10)
                .desc("열정 많은 사람 구해요")
                .processDesc("이렇게 할거에여")
                .mStartDate(LocalDateTime.of(2024, Month.APRIL, 1, 0, 0))
                .mEndDate(LocalDateTime.of(2030, Month.APRIL, 20, 0, 0))
                .leaderDesc("저는 이런 사람이에요.")
                .targetDesc("이런 사람이 왔으면 좋겠어요")
                .note("유의사항은 이거에요")
                .isMentorNeeded(true)
                .canJoinOnlyActiveGeneration(true)
                .createdGeneration(33)
                .targetActiveGeneration(33)
                .joinableParts(MeetingJoinablePart.values())
                .appliedInfo(new ArrayList<>())
                .build();

        Apply apply = Apply.builder()
                .meeting(meeting)
                .meetingId(1)
                .user(applyUser)
                .userId(2)
                .content("제 지원동기는요")
                .build();

        //meeting.addApply(apply);
        apply.updateApplyStatus(EnApplyStatus.APPROVE);

        applies = new ArrayList<>();
        applies.add(apply);
    }

    @Test
    void 내모임조회_성공() {
        // given
        User user = UserFixture.createStaticUser();
        user.setUserIdForTest(3);
        doReturn(Optional.of(user)).when(userRepository).findByOrgId(any());
        doReturn(applies).when(applyRepository).findAllByUserIdAndStatus(any(), any());

        MeetingV2GetAllMeetingByOrgUserQueryDto dto = new MeetingV2GetAllMeetingByOrgUserQueryDto(
                applyUser.getOrgId(), 1, 12);

        // when
        MeetingV2GetAllMeetingByOrgUserDto myMeetings = meetingV2Service.getAllMeetingByOrgUser(dto);

        // then
        Assertions.assertThat(myMeetings.getMeetings().get(0))
                .extracting("isMeetingLeader", "title", "category", "mStartDate", "mEndDate", "isActiveMeeting")
                .containsExactly(false, meeting.getTitle(), meeting.getCategory().getValue(), meeting.getMStartDate(),
                        meeting.getMEndDate(), true);
    }

    @Test
    public void 모임신청시_정원이찼을때_예외발생() {
        // given
        applies = new ArrayList<>();
        for (int i = 0; i < meeting.getCapacity(); i++) {
            User tempUser = User.builder()
                    .name("user" + i)
                    .orgId(2 + i)
                    .phone("010-0000-000" + i)
                    .build();
            tempUser.setUserIdForTest(3 + i); // 3부터 시작하는 userId 설정

            Apply apply = Apply.builder()
                    .meeting(meeting)
                    .meetingId(meeting.getId())
                    .user(tempUser)
                    .userId(tempUser.getId())
                    .content("꼭 하고 싶어요" + i)
                    .build();

            apply.updateApplyStatus(EnApplyStatus.APPROVE); // 승인 상태로 변경
            applies.add(apply);
        }

        MeetingV2ApplyMeetingDto requestBody = new MeetingV2ApplyMeetingDto(meeting.getId(), "열심히 하겠습니다.");

        doReturn(meeting).when(meetingRepository).findByIdOrThrow(requestBody.getMeetingId());
        doReturn(applyUser).when(userRepository).findByIdOrThrow(applyUser.getId());
        doReturn(applies).when(applyRepository).findAllByMeetingId(any());

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            meetingV2Service.applyMeeting(requestBody, applyUser.getId());
        });

        assertEquals(FULL_MEETING_CAPACITY.getErrorCode(), exception.getMessage());
    }

    @Test
    public void 모집시작시간이전_모임지원시_예외발생() {
        // given
        LocalDateTime oneMinuteAfterNow = LocalDateTime.now().plusMinutes(1);

        meeting = Meeting.builder()
                .user(ownerUser)
                .userId(ownerUser.getId())
                .title("사람 구해요")
                .category(MeetingCategory.STUDY)
                .startDate(oneMinuteAfterNow) // 모집 시작 시간을 현재 시간으로부터 1분 후로 설정
                .endDate(oneMinuteAfterNow.plusDays(1))
                .capacity(10)
                .desc("열정 많은 사람 구해요")
                .processDesc("이렇게 할거에여")
                .mStartDate(LocalDateTime.of(2028, Month.APRIL, 20, 0, 0))
                .mEndDate(LocalDateTime.of(2030, Month.APRIL, 20, 0, 0))
                .leaderDesc("저는 이런 사람이에요.")
                .targetDesc("이런 사람이 왔으면 좋겠어요")
                .note("유의사항은 이거에요")
                .isMentorNeeded(true)
                .canJoinOnlyActiveGeneration(true)
                .createdGeneration(33)
                .targetActiveGeneration(33)
                .joinableParts(MeetingJoinablePart.values())
                .appliedInfo(new ArrayList<>())
                .build();

        MeetingV2ApplyMeetingDto requestBody = new MeetingV2ApplyMeetingDto(meeting.getId(), "열심히 하겠습니다.");

        doReturn(meeting).when(meetingRepository).findByIdOrThrow(requestBody.getMeetingId());
        doReturn(applyUser).when(userRepository).findByIdOrThrow(applyUser.getId());

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            meetingV2Service.applyMeeting(requestBody, applyUser.getId());
        });

        assertEquals(NOT_IN_APPLY_PERIOD.getErrorCode(), exception.getMessage());
    }
}
