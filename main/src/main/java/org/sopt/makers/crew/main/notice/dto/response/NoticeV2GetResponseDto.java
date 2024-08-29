package org.sopt.makers.crew.main.notice.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class NoticeV2GetResponseDto {

	@Schema(description = "공지 id", example = "1")
	@NotNull
	private final Integer id;

	@Schema(description = "공지 제목", example = "공지 제목입니다")
	@NotNull
	private final String title;

	@Schema(description = "공지 부제목", example = "공지 부제목입니다")
	@NotNull
	private final String subTitle;

	@Schema(description = "공지 내용", example = "공지 내용입니다")
	@NotNull
	private final String contents;

	@Schema(description = "공지 생성 시각", example = "2024-07-30T15:30:00")
	@NotNull
	private final LocalDateTime createdDate;
}
