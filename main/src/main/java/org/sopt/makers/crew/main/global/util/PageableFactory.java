package org.sopt.makers.crew.main.global.util;

import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageableFactory {

	public static Pageable createPageable(PageOptionsDto queryCommand, boolean isUseAdPagination) {
		Sort sort = Sort.by(Sort.Direction.ASC, "id");
		return isUseAdPagination
			? new AdvertisementCustomPageable(queryCommand.getPage() - 1, sort)
			: new CustomPageable(queryCommand.getPage() - 1, queryCommand.getTake(), sort);
	}
}
