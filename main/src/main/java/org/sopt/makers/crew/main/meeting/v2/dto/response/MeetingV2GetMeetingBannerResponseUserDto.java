package org.sopt.makers.crew.main.meeting.v2.dto.response;

import org.sopt.makers.crew.main.entity.user.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(name = "MeetingV2GetMeetingBannerResponseUserDto", description = "모임 배너 유저 Dto")
public class MeetingV2GetMeetingBannerResponseUserDto {

	/** 개설자 crew ID */
	@Schema(description = "모임장 id, 크루에서 사용하는 userId", example = "1")
	@NotNull
	private final Integer id;

	/** 개설자 */
	@Schema(description = "모임장 이름", example = "홍길동")
	@NotNull
	private final String name;

	/** 개설자 playground ID */
	@Schema(description = "모임장 org id, 메이커스 프로덕트에서 범용적으로 사용하는 userId", example = "1")
	@NotNull
	private final Integer orgId;

	/** 프로필 사진 */
	@Schema(description = "모임장 프로필 사진", example = "[url] 형식")
	private final String profileImage;

	public static MeetingV2GetMeetingBannerResponseUserDto of(User meetingCreator) {
		return new MeetingV2GetMeetingBannerResponseUserDto(meetingCreator.getId(), meetingCreator.getName(),
			meetingCreator.getOrgId(), meetingCreator.getProfileImage());
	}
}
