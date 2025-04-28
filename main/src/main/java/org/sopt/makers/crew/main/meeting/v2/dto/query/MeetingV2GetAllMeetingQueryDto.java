package org.sopt.makers.crew.main.meeting.v2.dto.query;

import java.util.List;

import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.global.pagination.PaginationType;
import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeetingV2GetAllMeetingQueryDto extends PageOptionsDto {

	private List<String> category;

	private List<String> keyword;

	private List<String> status;

	@NotNull
	private Boolean isOnlyActiveGeneration;

	private MeetingJoinablePart[] joinableParts;

	private String query;

	private PaginationType paginationType = PaginationType.ADVERTISEMENT;

	public MeetingV2GetAllMeetingQueryDto(Integer page, Integer take) {
		super(page, take);
	}
}
