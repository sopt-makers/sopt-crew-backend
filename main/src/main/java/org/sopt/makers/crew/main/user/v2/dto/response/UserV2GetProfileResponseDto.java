package org.sopt.makers.crew.main.user.v2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class UserV2GetProfileResponseDto {
    /** 유저 ID */
    private Integer id;
    /** orgId */
    private Integer orgId;
    /** 유저 이름 */
    private String name;
    /** 유저 프로필 이미지 */
    private String profileImage;
    /** 활동 여부 */
    private Boolean hasActivities;
}
