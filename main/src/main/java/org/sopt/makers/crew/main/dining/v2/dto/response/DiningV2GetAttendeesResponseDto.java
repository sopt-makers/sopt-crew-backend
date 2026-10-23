package org.sopt.makers.crew.main.dining.v2.dto.response;

import java.util.List;

import org.sopt.makers.crew.main.entity.user.User;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 사용자용 회차 참석 현황 모달 목록")
public record DiningV2GetAttendeesResponseDto(List<Attendee> attendees) {

	public record Attendee(Integer id, String name, String profileImage) {
		public static Attendee of(User user) {
			return new Attendee(user.getId(), user.getName(), user.getProfileImage());
		}
	}
}
