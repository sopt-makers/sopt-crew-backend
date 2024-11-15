package org.sopt.makers.crew.main.internal.dto;

import java.util.List;

import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;

import lombok.Getter;

@Getter
public class InternalMeetingGetAllMeetingQueryDto extends PageOptionsDto {
	private final List<String> category;

	public InternalMeetingGetAllMeetingQueryDto(Integer page, Integer take, List<String> category) {
		super(page, take);
		this.category = category;
	}
}
