package org.sopt.makers.crew.main.meetingdemandcomment.v2;

import java.security.Principal;

import org.sopt.makers.crew.main.global.util.UserUtil;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.query.MeetingDemandCommentV2GetCommentsQueryDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.request.MeetingDemandCommentV2CreateCommentBodyDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.request.MeetingDemandCommentV2MentionUserInCommentRequestDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.request.MeetingDemandCommentV2UpdateCommentBodyDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandCommentV2CreateCommentResponseDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandCommentV2GetCommentsResponseDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandCommentV2ReportCommentResponseDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandCommentV2SwitchCommentLikeResponseDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandCommentV2UpdateCommentResponseDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.service.MeetingDemandCommentV2Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/meeting-demand/v2")
@RequiredArgsConstructor
public class MeetingDemandCommentV2Controller implements MeetingDemandCommentV2Api {

	private final MeetingDemandCommentV2Service meetingDemandCommentV2Service;

	@Override
	@GetMapping("/{meetingDemandId}/comments")
	public ResponseEntity<MeetingDemandCommentV2GetCommentsResponseDto> getComments(
		@PathVariable Integer meetingDemandId,
		@Valid @ModelAttribute @Parameter(hidden = true) MeetingDemandCommentV2GetCommentsQueryDto queryDto,
		Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.ok(meetingDemandCommentV2Service.getComments(meetingDemandId, queryDto, userId));
	}

	@Override
	@PostMapping("/{meetingDemandId}/comments")
	public ResponseEntity<MeetingDemandCommentV2CreateCommentResponseDto> createComment(
		@PathVariable Integer meetingDemandId,
		@Valid @RequestBody MeetingDemandCommentV2CreateCommentBodyDto requestBody,
		Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.ok(meetingDemandCommentV2Service.createComment(meetingDemandId, requestBody, userId));
	}

	@Override
	@PutMapping("/comments/{commentId}")
	public ResponseEntity<MeetingDemandCommentV2UpdateCommentResponseDto> updateComment(
		@PathVariable Integer commentId,
		@Valid @RequestBody MeetingDemandCommentV2UpdateCommentBodyDto requestBody,
		Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.ok(meetingDemandCommentV2Service.updateComment(commentId, requestBody.getContents(),
			userId));
	}

	@Override
	@DeleteMapping("/comments/{commentId}")
	public ResponseEntity<Void> deleteComment(@PathVariable Integer commentId, Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		meetingDemandCommentV2Service.deleteComment(commentId, userId);
		return ResponseEntity.ok().build();
	}

	@Override
	@PostMapping("/comments/{commentId}/like")
	public ResponseEntity<MeetingDemandCommentV2SwitchCommentLikeResponseDto> switchCommentLike(
		@PathVariable Integer commentId, Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.ok(meetingDemandCommentV2Service.switchCommentLike(commentId, userId));
	}

	@Override
	@PostMapping("/comments/mention")
	public ResponseEntity<Void> mentionUserInComment(
		@Valid @RequestBody MeetingDemandCommentV2MentionUserInCommentRequestDto requestBody,
		Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		meetingDemandCommentV2Service.mentionUserInComment(requestBody, userId);
		return ResponseEntity.ok().build();
	}

	@Override
	@PostMapping("/comments/{commentId}/report")
	public ResponseEntity<MeetingDemandCommentV2ReportCommentResponseDto> reportComment(
		@PathVariable Integer commentId, Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.ok(meetingDemandCommentV2Service.reportComment(commentId, userId));
	}
}
