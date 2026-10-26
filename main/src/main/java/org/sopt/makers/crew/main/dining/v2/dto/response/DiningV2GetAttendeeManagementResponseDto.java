package org.sopt.makers.crew.main.dining.v2.dto.response;

import java.util.List;

import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;
import org.sopt.makers.crew.main.global.util.UserUtil;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "관리자용 회차 참석자 목록")
public record DiningV2GetAttendeeManagementResponseDto(@NotNull List<Attendee> attendees, @NotNull PageMetaDto meta) {

	@Schema(name = "DiningV2ManagementAttendeeDto", description = "관리자용 참석자")
	public record Attendee(
		@Schema(description = "크루 사용자 ID") @NotNull Integer id,
		@Schema(description = "현재 참석자의 등록 시각 기준 번호 (오래된 순서부터 1번)") @NotNull long attendanceNumber,
		@NotNull String name,
		String profileImage,
		@Schema(description = "가장 최근 활동 기수") Integer recentGeneration,
		String phone
	) {
		public static Attendee of(User user, long attendanceNumber) {
			var recentActivity = UserUtil.getRecentUserActivity(user.getActivities());
			return new Attendee(user.getId(), attendanceNumber, user.getName(), user.getProfileImage(),
				recentActivity == null ? null : recentActivity.getGeneration(), user.getPhone());
		}
	}
}
