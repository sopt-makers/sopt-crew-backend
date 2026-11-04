package org.sopt.makers.crew.main.dining.v2.dto.response;

import java.util.List;

import org.sopt.makers.crew.main.entity.dining.DiningAttendance;
import org.sopt.makers.crew.main.entity.user.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "개설자 전용 승인된 공동 결제자 목록. 회차별로 반환하며 요청 시각 오름차순")
public record DiningV2GetApprovedPayersResponseDto(@NotNull List<ApprovedPayer> payers) {

	@Schema(name = "DiningV2ApprovedPayerDto", description = "회차별 승인된 공동 결제자")
	public record ApprovedPayer(@NotNull Integer userId, @NotNull String name, String profileImage,
		@NotNull Integer roundId, @NotNull int roundNumber) {
		public static ApprovedPayer of(DiningAttendance attendance, User user, int roundNumber) {
			return new ApprovedPayer(user.getId(), user.getName(), user.getProfileImage(), attendance.getRoundId(),
				roundNumber);
		}
	}
}
