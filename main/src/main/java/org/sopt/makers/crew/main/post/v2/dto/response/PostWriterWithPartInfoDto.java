package org.sopt.makers.crew.main.post.v2.dto.response;

import java.util.List;

import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;

import com.querydsl.core.annotations.QueryProjection;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(name = "PostWriterDetailInfoDto", description = "게시글 작성자 상세 Dto")
public class PostWriterWithPartInfoDto {

	@Schema(description = "작성자 id, 크루에서 사용하는 userId", example = "1")
	@NotNull
	private final Integer id;

	@Schema(description = "작성자 org id, 메이커스 프로덕트에서 범용적으로 사용하는 userId", example = "1")
	@NotNull
	private final Integer orgId;

	@Schema(description = "작성자 이름", example = "홍길동")
	@NotNull
	private final String name;

	@Schema(description = "작성자 프로필 사진", example = "[url] 형식")
	@NotNull
	private final String profileImage;

	@Schema(description = "작성자 기수, 파트 ", example = "36기 서버파트")
	private final List<UserActivityVO> partInfo;

	@QueryProjection
	public PostWriterWithPartInfoDto(Integer id, Integer orgId, String name, String profileImage,
		List<UserActivityVO> partInfo) {
		this.id = id;
		this.orgId = orgId;
		this.name = name;
		this.profileImage = profileImage;
		this.partInfo = partInfo;
	}

}
