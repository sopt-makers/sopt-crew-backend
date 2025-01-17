package org.sopt.makers.crew.main.lightning.v2.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "LightningV2CreateLightningBodyWithoutWelcomeMessageDto", description = "번쩍 모임 생성 및 수정 request body dto (환영 메시지 타입 제외)")
public record LightningV2CreateLightningBodyWithoutWelcomeMessageDto(
	@Schema(example = "알고보면 쓸데있는 개발 프로세스", description = "번쩍 모임 제목")
	@Size(min = 1, max = 30)
	@NotNull String title,

	@Schema(example = "api 가 터졌다고? 깃이 터졌다고?", description = "번쩍 소개")
	@Size(min = 1, max = 500)
	@NotNull
	String desc,

	@Schema(example = "협의 후 결정", description = "번쩍 일정 결정 방식")
	@NotNull
	String lightningTimingType,

	@Schema(example = "2025.10.29", description = "번쩍 활동 시작 날짜", name = "activityStartDate")
	@NotNull
	String activityStartDate,

	@Schema(example = "2025.10.30", description = "번쩍 활동 종료 날짜", name = "activityEndDate")
	@NotNull
	String activityEndDate,

	@Schema(example = "오프라인", description = "모임 장소 Tag")
	@NotNull
	String lightningPlaceType,

	@Schema(example = "잠실역 5번 출구", description = "모임 장소")
	@NotNull
	String lightningPlace,

	@Schema(example = "1", description = "최소 모집 인원")
	@Size(min = 1)
	@NotNull
	Integer minimumCapacity,

	@Schema(example = "5", description = "최대 모집 인원")
	@Size(max = 999)
	@NotNull
	Integer maximumCapacity,

	@Schema(example = "[\n"
		+ "    \"https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2023/04/12/7bd87736-b557-4b26-a0d5-9b09f1f1d7df\"\n"
		+ "  ]", description = "모임 이미지 리스트, 최대 1개")
	@NotEmpty
	@Size(max = 1)
	List<String> files
) {
}
