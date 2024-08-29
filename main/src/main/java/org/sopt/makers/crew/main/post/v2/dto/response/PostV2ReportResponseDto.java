package org.sopt.makers.crew.main.post.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "PostV2ReportResponseDto", description = "게시글 신고 응답 Dto")
public class PostV2ReportResponseDto {

	@Schema(description = "생성된 신고 id", example = "1")
	@NotNull
	private final Integer reportId;
}
