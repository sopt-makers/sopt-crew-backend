package org.sopt.makers.crew.main.global.pagination;

import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.springframework.data.domain.Pageable;

public interface PageableStrategy {
	Pageable createPageable(PageOptionsDto queryCommand);
}
