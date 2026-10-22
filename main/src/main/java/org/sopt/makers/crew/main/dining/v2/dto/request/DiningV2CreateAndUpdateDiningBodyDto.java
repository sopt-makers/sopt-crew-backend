package org.sopt.makers.crew.main.dining.v2.dto.request;

import java.util.List;

import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "DiningV2CreateAndUpdateDiningBodyDto", description = "회식 등록 및 수정 request body dto (1차 회차 정보 포함)")
public record DiningV2CreateAndUpdateDiningBodyDto(
	@Schema(example = "서버 파트 회식", description = "회식명")
	@NotBlank
	@Size(max = 30)
	String title,

	@Schema(example = "비틀비틀 비틀스 타코 서순라길 점", description = "1차 회식 장소 (선택)")
	@Size(max = 30)
	String place,

	@Schema(example = "https://naver.me/xKjC1aBc", description = "1차 네이버지도 링크 (선택)")
	@Size(max = 500)
	String naverMapLink,

	@Schema(example = "https://kko.to/aBcDeFg", description = "1차 카카오맵 링크 (선택)")
	@Size(max = 500)
	String kakaoMapLink,

	@Schema(example = "2026.10.30", description = "회식 일자 (yyyy.MM.dd)")
	@NotNull
	@Pattern(regexp = "^\\d{4}\\.\\d{2}\\.\\d{2}$")
	String diningDate,

	@Schema(example = "18:00", description = "1차 시작 시간 (HH:mm, 선택)")
	@Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$")
	String startTime,

	@Schema(example = "[\"SERVER\", \"WEB\"]", description = "참여 파트 목록. 전체 파트는 6개 모두 전달")
	@NotNull
	@Size(min = 1, max = 6)
	MeetingJoinablePart[] joinableParts,

	@Schema(example = "[\"https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2026/10/21/image.png\"]",
		description = "대표 이미지, 최대 1개. 비어 있으면 기본 이미지")
	@Size(max = 1)
	List<@NotBlank String> files
) {
}
