package org.sopt.makers.crew.main.meetingdemand.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "MeetingDemandCommentWriterDto", description = "모임 수요 댓글 익명 작성자 객체 Dto")
public class MeetingDemandCommentWriterDto {

	@Schema(description = "익명 닉네임", example = "성실한 판다")
	private final String anonymousNickname;

	@Schema(description = "익명 이미지 URL", example = "https://sopt-makers-mds.s3.ap-northeast-2.amazonaws.com/anonymousImage/avatar_m.png")
	private final String anonymousImageUrl;
}
