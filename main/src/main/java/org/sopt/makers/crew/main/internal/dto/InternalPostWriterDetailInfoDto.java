package org.sopt.makers.crew.main.internal.dto;

import java.util.Comparator;

import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.global.exception.ErrorStatus;
import org.sopt.makers.crew.main.global.exception.ServerException;
import org.sopt.makers.crew.main.post.v2.dto.response.PostWriterWithPartInfoDto;

import io.swagger.v3.oas.annotations.media.Schema;

public record InternalPostWriterDetailInfoDto(
	@Schema(description = "작성자 id, 크루에서 사용하는 userId", example = "1")
	Integer id,
	@Schema(description = "작성자 org id, 메이커스 프로덕트에서 범용적으로 사용하는 userId", example = "1")
	Integer orgId,
	@Schema(description = "작성자 이름", example = "홍길동")
	String name,
	@Schema(description = "작성자 프로필 사진", example = "[url] 형식")
	String profileImage,
	@Schema(description = "작성자 현재 기수, 파트 ", example = "36기 서버파트")
	UserActivityVO partInfo
) {
	public static InternalPostWriterDetailInfoDto from(PostWriterWithPartInfoDto postWriterWithPartInfoDto) {
		return new InternalPostWriterDetailInfoDto(
			postWriterWithPartInfoDto.getId(),
			postWriterWithPartInfoDto.getOrgId(),
			postWriterWithPartInfoDto.getName(),
			postWriterWithPartInfoDto.getProfileImage(),
			getRecentActivity(postWriterWithPartInfoDto)
		);
	}

	private static UserActivityVO getRecentActivity(PostWriterWithPartInfoDto postWriterWithPartInfoDto) {
		return postWriterWithPartInfoDto.getPartInfo().stream()
			.filter(userActivityVO -> userActivityVO.getPart() != null)
			.max(Comparator.comparingInt(UserActivityVO::getGeneration))
			.orElseThrow(() -> new ServerException(ErrorStatus.INTERNAL_SERVER_ERROR.getErrorCode()));
	}

}
