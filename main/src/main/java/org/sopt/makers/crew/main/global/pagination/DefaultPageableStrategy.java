package org.sopt.makers.crew.main.global.pagination;

import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.global.util.CustomPageable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class DefaultPageableStrategy implements PageableStrategy {

	@Override
	public Pageable createPageable(PageOptionsDto queryCommand) {
		Sort sort = Sort.by(Sort.Direction.ASC, "id");
		return new CustomPageable(queryCommand.getPage() - 1, queryCommand.getTake(), sort);
	}
}
