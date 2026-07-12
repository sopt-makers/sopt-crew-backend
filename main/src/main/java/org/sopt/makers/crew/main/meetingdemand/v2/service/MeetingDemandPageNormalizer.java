package org.sopt.makers.crew.main.meetingdemand.v2.service;

import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.springframework.stereotype.Component;

@Component
public class MeetingDemandPageNormalizer {

	public PageOptionsDto normalize(PageOptionsDto pageOptionsDto, int totalCount) {
		if (totalCount == 0) {
			return new PageOptionsDto(1, pageOptionsDto.getTake());
		}

		int pageCount = (int)Math.ceil((double)totalCount / pageOptionsDto.getTake());
		int normalizedPage = Math.min(pageOptionsDto.getPage(), pageCount);

		return new PageOptionsDto(normalizedPage, pageOptionsDto.getTake());
	}
}
