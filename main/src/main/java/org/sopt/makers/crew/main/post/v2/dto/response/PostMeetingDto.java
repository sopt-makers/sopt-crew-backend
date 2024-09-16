package org.sopt.makers.crew.main.post.v2.dto.response;

import com.querydsl.core.annotations.QueryProjection;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;

@Getter
@Schema(name = "PostMeetingDto", description = "게시글에 대한 모임 Dto")
public class PostMeetingDto {

	@Schema(description = "모임 id", example = "1")
	@NotNull
	private final Integer id;

	@Schema(description = "모임 제목", example = "모임 제목입니다.")
	@NotNull
	private final String title;

	@Schema(description = "모임 카테고리", example = "스터디")
	@NotNull
	private final String category;

	@Schema(description = "모임 이미지 url", example = "[url 형식]")
	@NotNull
	private final List<ImageUrlVO> imageURL;

	@Schema(description = "모임 설명", example = "모임 설명입니다.")
	@NotNull
	private final String desc;

	@QueryProjection
	public PostMeetingDto(Integer id, String title, MeetingCategory category, List<ImageUrlVO> imageURL, String desc) {
		this.id = id;
		this.title = title;
		this.category = category.getValue();
		this.imageURL = imageURL;
		this.desc = desc;
	}
}
