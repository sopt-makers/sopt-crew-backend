package org.sopt.makers.crew.main.comment.v2;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.security.Principal;

import lombok.RequiredArgsConstructor;

import org.sopt.makers.crew.main.comment.v2.dto.request.CommentV2CreateCommentBodyDto;
import org.sopt.makers.crew.main.comment.v2.dto.request.CommentV2MentionUserInCommentRequestDto;
import org.sopt.makers.crew.main.comment.v2.dto.request.CommentV2UpdateCommentBodyDto;
import org.sopt.makers.crew.main.comment.v2.dto.response.CommentV2CreateCommentResponseDto;
import org.sopt.makers.crew.main.comment.v2.dto.response.CommentV2ReportCommentResponseDto;
import org.sopt.makers.crew.main.comment.v2.dto.response.CommentV2UpdateCommentResponseDto;
import org.sopt.makers.crew.main.comment.v2.dto.response.CommentV2UpdateCommentResponseDto;
import org.sopt.makers.crew.main.comment.v2.service.CommentV2Service;
import org.sopt.makers.crew.main.common.util.UserUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comment/v2")
@RequiredArgsConstructor
@Tag(name = "댓글/대댓글")
public class CommentV2Controller implements CommentV2Api {

	private final CommentV2Service commentV2Service;

	@Override
	@PostMapping()
	public ResponseEntity<CommentV2CreateCommentResponseDto> createComment(
		@Valid @RequestBody CommentV2CreateCommentBodyDto requestBody, Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.ok(commentV2Service.createComment(requestBody, userId));
	}

	@Override
	@PutMapping("/{commentId}")
	public ResponseEntity<CommentV2UpdateCommentResponseDto> updateComment(
		@PathVariable Integer commentId,
		@Valid @RequestBody CommentV2UpdateCommentBodyDto requestBody,
		Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.ok(
			commentV2Service.updateComment(commentId, requestBody.getContents(), userId));
	}

	@Override
	@PostMapping("/{commentId}/report")
	public ResponseEntity<CommentV2ReportCommentResponseDto> reportComment(
		@PathVariable Integer commentId, Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.ok(commentV2Service.reportComment(commentId, userId));
	}

	@Override
	@DeleteMapping("/{commentId}")
	public ResponseEntity<Void> deleteComment(
		Principal principal,
		@PathVariable Integer commentId) {
		Integer userId = UserUtil.getUserId(principal);

		commentV2Service.deleteComment(commentId, userId);

		return ResponseEntity.noContent().build();
	}

	@Override
	@PostMapping("/mention")
	public ResponseEntity<Void> mentionUserInComment(
		@Valid @RequestBody CommentV2MentionUserInCommentRequestDto requestBody,
		Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		commentV2Service.mentionUserInComment(requestBody, userId);
		return ResponseEntity.status(HttpStatus.OK).build();
	}
}
