package org.sopt.makers.crew.main.global.pagination;

import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;
import org.springframework.data.domain.Pageable;

public interface PageableStrategy {
	Pageable createPageable(PageOptionsDto queryCommand);

	int calculatePageCount(int itemCount, int take);

	default int normalizePage(PageOptionsDto queryCommand, int itemCount) {
		int pageCount = calculatePageCount(itemCount, queryCommand.getTake());

		if (pageCount == 0) {
			return 1;
		}

		return Math.min(queryCommand.getPage(), pageCount);
	}

	default PageMetaDto createPageMeta(Pageable pageable, int itemCount, int take) {
		return new PageMetaDto(
			pageable.getPageNumber() + 1,
			pageable.getPageSize(),
			itemCount,
			calculatePageCount(itemCount, take)
		);
	}
}
