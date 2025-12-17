package org.sopt.makers.crew.main.soptmap.service.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SubwayStationDto {

	@JsonIgnore
	private final Long id;
	@Schema(description = "지하철역", example = "강남역")
	private final String name;
	@Schema(description = "호선", example = "[1호선, 2호선]")
	private final List<String> subwayLines;

}
