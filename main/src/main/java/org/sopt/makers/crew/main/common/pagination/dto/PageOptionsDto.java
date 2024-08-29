package org.sopt.makers.crew.main.common.pagination.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class PageOptionsDto {

	@Schema(description = "각 페이지", defaultValue = "1", example = "1")
	@Min(1)
	private Integer page;

	@Schema(description = "가져올 데이터 개수", defaultValue = "12", example = "12")
	@Min(1)
	@Max(50)
	private Integer take;

	public PageOptionsDto(Integer page, Integer take) {
		this.page = page == null ? 1 : page;
		this.take = take == null ? 12 : take;
	}
}
