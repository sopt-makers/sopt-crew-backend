package org.sopt.makers.crew.main.meeting.v2.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;

@Getter
public class ApplicantDto {
    private final Integer id;
    private final String name;
    private final Integer orgId;
    private final UserActivityVO recentActivity;
    private final String profileImage;
    private final String phone;

    @QueryProjection
    public ApplicantDto(Integer id, String name, Integer orgId, List<UserActivityVO> userActivityVOs, String profileImage,
                        String phone) {

        this.id = id;
        this.name = name;
        this.orgId = orgId;
        this.recentActivity = userActivityVOs.stream().max(Comparator.comparingInt(UserActivityVO::getGeneration)).orElse(null);
        this.profileImage = profileImage;
        this.phone = phone;
    }
}
