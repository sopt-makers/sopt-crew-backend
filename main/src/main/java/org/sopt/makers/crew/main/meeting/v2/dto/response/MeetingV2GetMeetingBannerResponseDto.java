package org.sopt.makers.crew.main.meeting.v2.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class MeetingV2GetMeetingBannerResponseDto {

    /** 모임 ID */
    private Integer id;
    /** 유저 Crew ID */
    private Integer userId;
    /** 모임 제목 */
    private String title;
    /**
     * 모임 카테고리
     * 
     * @apiNote '스터디', '행사'
     */
    private MeetingCategory category;
    /**
     * 썸네일 이미지
     * 
     * @apiNote 여러개여도 첫번째 이미지만 사용
     */
    private List<ImageUrlVO> imageURL;
    /** 모임 활동 시작일 */
    private LocalDateTime mStartDate;
    /** 모임 활동 종료일 */
    private LocalDateTime mEndDate;
    /** 모임 모집 시작일 */
    private LocalDateTime startDate;
    /** 모임 모집 종료일 */
    private LocalDateTime endDate;
    /** 모임 인원 */
    private Integer capacity;
    /** 최근 활동 일자 */
    private Optional<LocalDateTime> recentActivityDate;
    /** 모임 타겟 기수 */
    private Integer targetActiveGeneration;
    /** 모임 타겟 파트 */
    private MeetingJoinablePart[] joinableParts;
    /** 지원자 수 */
    private Integer applicantCount;
    /** 가입된 지원자 수 */
    private Integer approvedUserCount;
    /** 개설자 정보 */
    private MeetingV2GetMeetingBannerResponseUserDto user;
    /** 미팅 상태 */
    private Integer status;
}
