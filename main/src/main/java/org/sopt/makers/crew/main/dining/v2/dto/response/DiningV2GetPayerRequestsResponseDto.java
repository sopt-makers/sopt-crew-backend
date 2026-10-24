package org.sopt.makers.crew.main.dining.v2.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.sopt.makers.crew.main.entity.dining.DiningAttendance;
import org.sopt.makers.crew.main.entity.user.User;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "개설자 전용 공동 결제자 대기 요청 목록. 요청 시각 오름차순")
public record DiningV2GetPayerRequestsResponseDto(List<PayerRequest> requests) {

	public record PayerRequest(Integer userId, String name, String profileImage, Integer roundId,
		int roundNumber, LocalDateTime requestedAt) {
		public static PayerRequest of(DiningAttendance attendance, User user, int roundNumber) {
			return new PayerRequest(user.getId(), user.getName(), user.getProfileImage(), attendance.getRoundId(),
				roundNumber, attendance.getPayerRequestedAt());
		}
	}
}
