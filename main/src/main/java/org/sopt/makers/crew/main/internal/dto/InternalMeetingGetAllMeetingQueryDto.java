package org.sopt.makers.crew.main.internal.dto;

import java.util.List;

import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(name = "InternalMeetingGetAllMeetingQueryDto", description = "[Internal] 모임 조회 요청 Dto")
public class InternalMeetingGetAllMeetingQueryDto extends PageOptionsDto {
	@Schema(
		description = "모임 종류",
		example = "스터디",
		allowableValues = {"스터디", "행사", "세미나"})
	private final List<String> category;

	public InternalMeetingGetAllMeetingQueryDto(Integer page, Integer take, List<String> category) {
		super(page, take);
		this.category = category;
	}
}
