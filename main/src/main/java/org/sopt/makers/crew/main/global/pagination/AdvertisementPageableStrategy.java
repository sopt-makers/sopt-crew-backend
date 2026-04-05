package org.sopt.makers.crew.main.global.pagination;

import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.global.util.AdvertisementCustomPageable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class AdvertisementPageableStrategy implements PageableStrategy {
	private static final int FIRST_PAGE_SIZE = 11;
	private static final int OTHER_PAGE_SIZE = 12;

	@Override
	public Pageable createPageable(PageOptionsDto queryCommand) {
		Sort sort = Sort.by(Sort.Direction.ASC, "id");
		return new AdvertisementCustomPageable(queryCommand.getPage() - 1, sort);
	}

	@Override
	public int calculatePageCount(int itemCount, int take) {
		if (itemCount == 0) {
			return 0;
		}

		if (itemCount <= FIRST_PAGE_SIZE) {
			return 1;
		}

		return 1 + (int)Math.ceil((double)(itemCount - FIRST_PAGE_SIZE) / OTHER_PAGE_SIZE);
	}
}
