package org.sopt.makers.crew.main.meeting.v2.dto.response;

import java.time.LocalDateTime;

import org.sopt.makers.crew.main.entity.apply.Apply;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "모임 같은 파트/기수 신청 정보 조회 dto")
public class ApplyMemberInfoDto {

	@Schema(description = "신청 id", example = "3")
	@NotNull
	private final Integer id;

	@Schema(description = "신청 번호", example = "1")
	@NotNull
	private final Integer applyNumber;

	@Schema(description = "신청 타입", example = "0")
	@NotNull
	private final Integer type;

	@Schema(description = "모임 id", example = "13")
	@NotNull
	private final Integer meetingId;

	@Schema(description = "신청자 id", example = "184")
	@NotNull
	private final Integer userId;

	@Schema(description = "신청 날짜 및 시간", example = "2024-10-13T23:59:59")
	@NotNull
	private final LocalDateTime appliedDate;

	@Schema(description = "신청 상태", example = "1")
	@NotNull
	private final Integer status;

	@Schema(description = "신청자 객체", example = "")
	@NotNull
	private final ApplicantByMeetingDto user;

	public static ApplyMemberInfoDto of(Apply apply, Integer applyNumber) {
		ApplicantByMeetingDto applicantByMeetingDto = ApplicantByMeetingDto.of(apply.getUser());

		return new ApplyMemberInfoDto(apply.getId(), applyNumber, apply.getType().getValue(), apply.getMeetingId(),
			apply.getUserId(), apply.getAppliedDate(), apply.getStatus().getValue(), applicantByMeetingDto);
	}
}
