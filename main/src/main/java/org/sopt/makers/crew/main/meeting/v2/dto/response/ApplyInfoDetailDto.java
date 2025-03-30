package org.sopt.makers.crew.main.meeting.v2.dto.response;

import java.time.LocalDateTime;

import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Schema(name = "ApplyInfoDetailDto", description = "모임 신청 객체 Dto 상세 -> 신청 번호 추가")
@RequiredArgsConstructor
public class ApplyInfoDetailDto {

	@Schema(description = "신청 id", example = "1")
	@NotNull
	private final Integer id;

	@Schema(description = "신청 번호", example = "1")
	@NotNull
	private final Integer applyNumber;

	@Schema(description = "전하는 말", example = "저 뽑아주세요.")
	private final String content;

	@Schema(description = "신청 시간", example = "2024-07-30T15:30:00")
	@NotNull
	private final LocalDateTime appliedDate;

	@Schema(description = "신청 상태", example = "1")
	@NotNull
	private final EnApplyStatus status;

	@Schema(description = "신청자 정보", example = "")
	@NotNull
	private final ApplicantDto user;

	public static ApplyInfoDetailDto toApplyInfoDetailDto(ApplyInfoDto applyInfoDto, Integer applyNumber) {
		return new ApplyInfoDetailDto(applyInfoDto.getId(), applyNumber, applyInfoDto.getContent(),
			applyInfoDto.getAppliedDate(),
			applyInfoDto.getStatus(), applyInfoDto.getUser());
	}

}
