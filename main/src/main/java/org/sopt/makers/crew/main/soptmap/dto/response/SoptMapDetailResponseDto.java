package org.sopt.makers.crew.main.soptmap.dto.response;

import java.util.List;

import org.sopt.makers.crew.main.entity.soptmap.MapTag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(name = "SoptMapDetailResponseDto", description = "솝맵 상세조회 응답 Dto")
@Getter
@RequiredArgsConstructor
@Builder
public class SoptMapDetailResponseDto {

    @Schema(description = "솝맵 ID", example = "1")
    private final Long id;

    @Schema(description = "장소 이름", example = "온더플랜")
    private final String placeName;

    @Schema(description = "한줄 소개", example = "장소 너무 좋아요")
    private final String description;

    @Schema(description = "장소 태그", example = "[\"FOOD\", \"CAFE\"]")
    private final List<MapTag> tags;

    @Schema(description = "주변 지하철역 이름", example = "[\"강남역\", \"건대입구역\"]")
    private final List<String> stationNames;

    @Schema(description = "추천 수", example = "5")
    private final Long recommendCount;

    @Schema(description = "현재 유저의 추천 여부", example = "true")
    private final Boolean isRecommended;

    @Schema(description = "kakao 맵 링크", example = "https://~~~")
    private final String kakaoLink;

    @Schema(description = "naver 맵 링크", example = "https://~~~")
    private final String naverLink;

    @Schema(description = "장소 등록한 사람의 이름", example = "김효준")
    private final String creatorName;

    @Schema(description = "내가 등록했는지", example = "false")
    private final Boolean isCreator;
}
