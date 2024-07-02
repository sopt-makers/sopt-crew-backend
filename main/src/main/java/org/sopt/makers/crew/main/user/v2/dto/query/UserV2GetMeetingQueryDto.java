package org.sopt.makers.crew.main.user.v2.dto.query;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.user.User;

@Getter
public class UserV2GetMeetingQueryDto {

    private final Integer id;
    private final User user;
    private final Integer userId;
    private final String title;
    private final MeetingCategory category;
    private final List<ImageUrlVO> imageURL;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final Integer capacity;
    private final String desc;
    private final String processDesc;
    private final LocalDateTime mStartDate;
    private final LocalDateTime mEndDate;
    private final String leaderDesc;
    private final String targetDesc;
    private final String note;
    private final Boolean isMentorNeeded;
    private final Boolean canJoinOnlyActiveGeneration;
    private final Integer createdGeneration;
    private final Integer targetActiveGeneration;

    @QueryProjection
    public UserV2GetMeetingQueryDto(Integer id, User user, Integer userId, String title,
        MeetingCategory category,
        List<ImageUrlVO> imageURL, LocalDateTime startDate, LocalDateTime endDate, Integer capacity,
        String desc, String processDesc, LocalDateTime mStartDate, LocalDateTime mEndDate,
        String leaderDesc, String targetDesc, String note, Boolean isMentorNeeded,
        Boolean canJoinOnlyActiveGeneration, Integer createdGeneration,
        Integer targetActiveGeneration) {
        this.id = id;
        this.user = user;
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.imageURL = imageURL;
        this.startDate = startDate;
        this.endDate = endDate;
        this.capacity = capacity;
        this.desc = desc;
        this.processDesc = processDesc;
        this.mStartDate = mStartDate;
        this.mEndDate = mEndDate;
        this.leaderDesc = leaderDesc;
        this.targetDesc = targetDesc;
        this.note = note;
        this.isMentorNeeded = isMentorNeeded;
        this.canJoinOnlyActiveGeneration = canJoinOnlyActiveGeneration;
        this.createdGeneration = createdGeneration;
        this.targetActiveGeneration = targetActiveGeneration;
    }
}
