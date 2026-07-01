package org.sopt.makers.crew.main.meetingdemandcomment.v2;

import java.security.Principal;

import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.query.MeetingDemandCommentV2GetCommentsQueryDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.request.MeetingDemandCommentV2CreateCommentBodyDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.request.MeetingDemandCommentV2MentionUserInCommentRequestDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.request.MeetingDemandCommentV2UpdateCommentBodyDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandCommentV2CreateCommentResponseDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandCommentV2GetCommentsResponseDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandCommentV2ReportCommentResponseDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandCommentV2SwitchCommentLikeResponseDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandCommentV2UpdateCommentResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "모임 수요 댓글")
public interface MeetingDemandCommentV2Api {

	@Operation(summary = "모임 수요 댓글 조회", description = "모임 수요 댓글과 대댓글 목록을 조회")
	@ApiResponse(responseCode = "200", description = "성공")
	@Parameters({
		@Parameter(name = "page", description = "페이지, default = 1", example = "1", schema = @Schema(type = "integer", format = "int32")),
		@Parameter(name = "take", description = "가져올 데이터 개수, default = 12", example = "12", schema = @Schema(type = "integer", format = "int32"))
	})
	ResponseEntity<MeetingDemandCommentV2GetCommentsResponseDto> getComments(
		@PathVariable Integer meetingDemandId,
		@Valid @ModelAttribute @Parameter(hidden = true) MeetingDemandCommentV2GetCommentsQueryDto queryDto,
		Principal principal);

	@Operation(summary = "모임 수요 댓글 작성", description = "모임 수요 댓글 또는 대댓글 생성")
	@ApiResponse(responseCode = "200", description = "성공")
	ResponseEntity<MeetingDemandCommentV2CreateCommentResponseDto> createComment(
		@PathVariable Integer meetingDemandId,
		@Valid @RequestBody MeetingDemandCommentV2CreateCommentBodyDto requestBody,
		Principal principal);

	@Operation(summary = "모임 수요 댓글 수정", description = "본인이 작성한 모임 수요 댓글 수정")
	@ApiResponse(responseCode = "200", description = "성공")
	ResponseEntity<MeetingDemandCommentV2UpdateCommentResponseDto> updateComment(
		@PathVariable Integer commentId,
		@Valid @RequestBody MeetingDemandCommentV2UpdateCommentBodyDto requestBody,
		Principal principal);

	@Operation(summary = "모임 수요 댓글 삭제", description = "본인이 작성한 모임 수요 댓글 삭제")
	@ApiResponse(responseCode = "200", description = "성공")
	ResponseEntity<Void> deleteComment(@PathVariable Integer commentId, Principal principal);

	@Operation(summary = "모임 수요 댓글 좋아요 토글", description = "모임 수요 댓글의 좋아요 상태를 토글")
	@ApiResponse(responseCode = "200", description = "성공")
	ResponseEntity<MeetingDemandCommentV2SwitchCommentLikeResponseDto> switchCommentLike(
		@PathVariable Integer commentId, Principal principal);

	@Operation(summary = "모임 수요 댓글 멘션 알림", description = "모임 수요 댓글에서 멘션된 사용자에게 푸시 알림 전송")
	@ApiResponse(responseCode = "200", description = "성공")
	ResponseEntity<Void> mentionUserInComment(
		@Valid @RequestBody MeetingDemandCommentV2MentionUserInCommentRequestDto requestBody,
		Principal principal);

	@Operation(summary = "모임 수요 댓글 신고", description = "다른 사람이 작성한 모임 수요 댓글을 신고")
	@ApiResponse(responseCode = "200", description = "성공")
	ResponseEntity<MeetingDemandCommentV2ReportCommentResponseDto> reportComment(
		@PathVariable Integer commentId, Principal principal);
}
