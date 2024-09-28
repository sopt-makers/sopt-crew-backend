package org.sopt.makers.crew.main.global.pagination.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PageMetaDto {

	@Schema(description = "페이지 위치")
	@NotNull
	private int page;

	@Schema(description = "가져올 데이터 개수")
	@NotNull
	private int take;

	@Schema(description = "응답 데이터 개수")
	@NotNull
	private int itemCount;

	@Schema(description = "총 페이지 수")
	@NotNull
	private int pageCount;

	@Schema(description = "이전 페이지가 있는지 유무")
	@NotNull
	private boolean hasPreviousPage;

	@Schema(description = "다음 페이지가 있는지 유무")
	@NotNull
	private boolean hasNextPage;

	public PageMetaDto(PageOptionsDto pageOptionsDto, int itemCount) {
		this.page = pageOptionsDto.getPage();
		this.take = pageOptionsDto.getTake();
		this.itemCount = itemCount;
		this.pageCount = (int)Math.ceil((double)this.itemCount / this.take);
		this.hasPreviousPage = this.page > 1;
		this.hasNextPage = this.page < this.pageCount;
	}
}
