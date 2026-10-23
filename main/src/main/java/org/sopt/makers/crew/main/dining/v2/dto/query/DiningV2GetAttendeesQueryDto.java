package org.sopt.makers.crew.main.dining.v2.dto.query;

import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class DiningV2GetAttendeesQueryDto extends PageOptionsDto {

	@Schema(description = "등록 시각 정렬 방향", defaultValue = "desc", allowableValues = {"asc", "desc"})
	@Pattern(regexp = "asc|desc")
	private final String date;

	public DiningV2GetAttendeesQueryDto(Integer page, Integer take, String date) {
		super(page, take == null ? 10 : take);
		this.date = date == null ? "desc" : date;
	}

	@Override
	@Schema(description = "가져올 데이터 개수", defaultValue = "10", allowableValues = {"10", "30", "50"})
	public Integer getTake() {
		return super.getTake();
	}

	@AssertTrue(message = "조회 개수는 10, 30, 50 중 하나여야 합니다.")
	public boolean isValidTake() {
		return getTake() == 10 || getTake() == 30 || getTake() == 50;
	}
}
