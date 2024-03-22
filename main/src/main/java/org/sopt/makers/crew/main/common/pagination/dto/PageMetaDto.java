package org.sopt.makers.crew.main.common.pagination.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class PageMetaDto {

  @Schema(description = "페이지 위치")
  private int page;

  @Schema(description = "가져올 데이터 개수")
  private int take;

  @Schema(description = "응답 데이터 개수")
  private int itemCount;

  @Schema(description = "총 페이지 수")
  private int pageCount;

  @Schema(description = "이전 페이지가 있는지 유무")
  private boolean hasPreviousPage;

  @Schema(description = "다음 페이지가 있는지 유무")
  private boolean hasNextPage;

  public PageMetaDto(PageOptionsDto pageOptionsDto, int itemCount) {
    this.page = pageOptionsDto.getPage();
    this.take = pageOptionsDto.getTake();
    this.itemCount = itemCount;
    this.pageCount = (int) Math.ceil((double) this.itemCount / this.take);
    this.hasPreviousPage = this.page > 1;
    this.hasNextPage = this.page < this.pageCount;
  }
}
