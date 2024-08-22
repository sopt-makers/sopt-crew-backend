package org.sopt.makers.crew.main.meeting.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "모임 지원자 상태 변경 request body dto")
public class ApplyV2UpdateStatusBodyDto {
	@Schema(example = "1", description = "신청/지원 id")
	@NotNull
	private final Integer applyId;

	@Schema(example = "0", description = "0: 대기, 1: 승인, 2: 거절")
	@NotNull
	private final Integer status;

}
