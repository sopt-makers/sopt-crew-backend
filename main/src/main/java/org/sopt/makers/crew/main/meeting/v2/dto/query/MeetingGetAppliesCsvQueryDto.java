package org.sopt.makers.crew.main.meeting.v2.dto.query;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "모임 지원자 csv 파일 요청 dto")
public class MeetingGetAppliesCsvQueryDto {

	@NotNull
	@Schema(example = "[0,1]", description = "0: 대기, 1: 승인된 신청자, 2: 거절된 신청자")
	private List<Integer> status;

	@NotNull
	@Schema(example = "0", description = "0: 지원, 1: 초대")
	private List<Integer> type;

	@NotNull
	@Schema(example = "asc", description = "정렬순")
	private String order;

	public MeetingGetAppliesCsvQueryDto(List<Integer> status, List<Integer> type, String order) {
		this.status = status;
		this.type = type;
		this.order = order;
	}
}
