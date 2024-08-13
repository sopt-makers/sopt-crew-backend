package org.sopt.makers.crew.main.meeting.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "MeetingV2GetMeetingBannerResponseUserDto", description = "모임 배너 유저 Dto")
public class MeetingV2GetMeetingBannerResponseUserDto {

	/** 개설자 crew ID */
	@Schema(description = "모임장 id, 크루에서 사용하는 userId", example = "1")
	@NotNull
	private Integer id;

	/** 개설자 */
	@Schema(description = "모임장 이름", example = "홍길동")
	@NotNull
	private String name;

	/** 개설자 playground ID */
	@Schema(description = "모임장 org id, 메이커스 프로덕트에서 범용적으로 사용하는 userId", example = "1")
	@NotNull
	private Integer orgId;

	/** 프로필 사진 */
	@Schema(description = "모임장 프로필 사진", example = "[url] 형식")
	private String profileImage;
}
