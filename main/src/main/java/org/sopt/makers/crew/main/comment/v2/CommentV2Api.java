package org.sopt.makers.crew.main.comment.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

import java.security.Principal;

import org.sopt.makers.crew.main.comment.v2.dto.request.CommentV2CreateCommentBodyDto;
import org.sopt.makers.crew.main.comment.v2.dto.request.CommentV2MentionUserInCommentRequestDto;
import org.sopt.makers.crew.main.comment.v2.dto.request.CommentV2UpdateCommentBodyDto;
import org.sopt.makers.crew.main.comment.v2.dto.response.CommentV2CreateCommentResponseDto;
import org.sopt.makers.crew.main.comment.v2.dto.response.CommentV2ReportCommentResponseDto;
import org.sopt.makers.crew.main.comment.v2.dto.response.CommentV2UpdateCommentResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

public interface CommentV2Api {

	@Operation(summary = "모임 게시글 댓글 작성")
	@ResponseStatus(HttpStatus.CREATED)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "성공"),
	})
	ResponseEntity<CommentV2CreateCommentResponseDto> createComment(
		@Valid @RequestBody CommentV2CreateCommentBodyDto requestBody, Principal principal);

	@Operation(summary = "모임 게시글 댓글 수정")
	@ResponseStatus(HttpStatus.OK)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "성공"),
	})
	ResponseEntity<CommentV2UpdateCommentResponseDto> updateComment(
		@PathVariable Integer commentId,
		@Valid @RequestBody CommentV2UpdateCommentBodyDto requestBody,
		Principal principal);

	@Operation(summary = "댓글 신고하기")
	@ResponseStatus(HttpStatus.CREATED)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "성공"),
	})
	ResponseEntity<CommentV2ReportCommentResponseDto> reportComment(
		@PathVariable Integer commentId, Principal principal);

	@Operation(summary = "모임 게시글 댓글 삭제")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "성공"),
	})
	ResponseEntity<Void> deleteComment(Principal principal, @PathVariable Integer commentId);

	@Operation(summary = "댓글에서 유저 멘션")
	@ResponseStatus(HttpStatus.OK)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "성공"),
	})
	ResponseEntity<Void> mentionUserInComment(
		@Valid @RequestBody CommentV2MentionUserInCommentRequestDto requestBody,
		Principal principal);
}
