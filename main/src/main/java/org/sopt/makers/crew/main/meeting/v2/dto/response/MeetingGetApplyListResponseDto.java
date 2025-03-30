package org.sopt.makers.crew.main.meeting.v2.dto.response;

import java.util.List;

import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "MeetingGetApplyListResponseDto", description = "모임 신청 목록 응답 Dto")
public class MeetingGetApplyListResponseDto {

	@Schema(description = "신청 목록", example = "")
	@NotNull
	private final List<ApplyInfoDetailDto> apply;

	@NotNull
	private final PageMetaDto meta;

}
