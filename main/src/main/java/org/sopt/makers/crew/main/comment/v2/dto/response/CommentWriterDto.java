package org.sopt.makers.crew.main.comment.v2.dto.response;

import com.querydsl.core.annotations.QueryProjection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(name = "CommentWriterDto", description = "댓글 작성자 객체 Dto", required = true)
public class CommentWriterDto {

	@Schema(description = "댓글 id", example = "1")
	private final Integer id;

	@Schema(description = "댓글 org id", example = "2")
	private final Integer orgId;

	@Schema(description = "댓글 작성자 이름", example = "홍길동")
	private final String name;

	@Schema(description = "댓글 작성자 프로필 사진", example = "[url] 형식")
	private final String profileImage;

	@QueryProjection
	public CommentWriterDto(Integer id, Integer orgId, String name, String profileImage) {
		this.id = id;
		this.orgId = orgId;
		this.name = name;
		this.profileImage = profileImage;
	}
}
