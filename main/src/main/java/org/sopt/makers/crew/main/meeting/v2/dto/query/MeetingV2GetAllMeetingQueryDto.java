package org.sopt.makers.crew.main.meeting.v2.dto.query;

import java.util.List;

import org.sopt.makers.crew.main.common.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeetingV2GetAllMeetingQueryDto extends PageOptionsDto {


	List<String> category;

	List<String> status;
	@NotNull
	Boolean isOnlyActiveGeneration;
	@NotNull
	MeetingJoinablePart[] joinableParts;
	@NotNull
	String query;

	public MeetingV2GetAllMeetingQueryDto(Integer page, Integer take) {
		super(page, take);
	}
}
