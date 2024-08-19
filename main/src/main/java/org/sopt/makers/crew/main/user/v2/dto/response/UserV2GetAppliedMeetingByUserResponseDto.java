package org.sopt.makers.crew.main.user.v2.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "UserV2GetAppliedMeetingByUserResponseDto", description = "내가 신청한 모임 조회 Dto")
public record UserV2GetAppliedMeetingByUserResponseDto(
	@Schema(description = "내가 신청한 모임 정보")
	@NotNull
	List<ApplyV2GetAppliedMeetingByUserResponseDto> apply,
	@Schema(description = "내가 신청한 모임 갯수")
	@NotNull
	Integer count
) {
	public static UserV2GetAppliedMeetingByUserResponseDto of(List<ApplyV2GetAppliedMeetingByUserResponseDto> apply){
		return new UserV2GetAppliedMeetingByUserResponseDto(apply, apply.size());
	}
}
