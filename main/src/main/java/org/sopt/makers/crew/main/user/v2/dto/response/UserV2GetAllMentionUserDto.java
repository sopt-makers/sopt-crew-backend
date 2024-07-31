package org.sopt.makers.crew.main.user.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "UserV2GetAllMentionUserDto", description = "멘션 유저 조회 응답 Dto")
public class UserV2GetAllMentionUserDto {
    /**
     * 주의!! : 필드명은 userId 이지만 실제 응답 해야하는 데이터는 orgId 입니다.
     */
    @Schema(description = "유저 id", example = "1")
    private final Integer userId;

    @Schema(description = "유저 이름", example = "홍길")
    private final String userName;

    @Schema(description = "최근 파트", example = "서버")
    private final String recentPart;

    @Schema(description = "최근 기수", example = "33")
    private final int recentGeneration;

    @Schema(description = "유저 프로필 사진", example = "[url] 형식")
    private final String profileImageUrl;
}

