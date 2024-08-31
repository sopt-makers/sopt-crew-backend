package org.sopt.makers.crew.main.user.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "UserV2GetAllMeetingByUserMeetingDto", description = "내가 속한 모임 조회 응답 Dto")
public class UserV2GetAllMeetingByUserMeetingDto {

	@Schema(description = "모임 id", example = "1")
	@NotNull
	private int id;

	@Schema(description = "모임 제목", example = "모임 제목입니다.")
	@NotNull
	private String title;

	@Schema(description = "모임 내용", example = "모임 내용입니다.")
	@NotNull
	private String contents;

	@Schema(description = "모임 사진", example = "[url] 형식")
	@NotNull
	private String imageUrl;

	@Schema(description = "모임 카테고리", example = "스터디")
	@NotNull
	private String category;
}
