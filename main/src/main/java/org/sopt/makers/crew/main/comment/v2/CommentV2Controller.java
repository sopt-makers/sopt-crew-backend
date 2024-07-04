package org.sopt.makers.crew.main.comment.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.comment.v2.dto.request.CommentV2CreateCommentBodyDto;
import org.sopt.makers.crew.main.comment.v2.dto.request.CommentV2MentionUserInCommentRequestDto;
import org.sopt.makers.crew.main.comment.v2.dto.response.CommentV2CreateCommentResponseDto;
import org.sopt.makers.crew.main.comment.v2.service.CommentV2Service;
import org.sopt.makers.crew.main.common.util.UserUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comment/v2")
@RequiredArgsConstructor
@Tag(name = "댓글/대댓글")
public class CommentV2Controller {

    private final CommentV2Service commentV2Service;

    @Operation(summary = "모임 게시글 댓글 작성")
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "성공"),
    })
    public ResponseEntity<CommentV2CreateCommentResponseDto> createComment(
        @Valid @RequestBody CommentV2CreateCommentBodyDto requestBody, Principal principal) {
        Integer userId = UserUtil.getUserId(principal);
        return ResponseEntity.ok(commentV2Service.createComment(requestBody, userId));
    }

    @Operation(summary = "댓글에서 유저 멘션")
    @PostMapping("/mention")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공"),
    })
    public ResponseEntity<Void> mentionUserInComment(
        @Valid @RequestBody CommentV2MentionUserInCommentRequestDto requestBody,
        Principal principal) {
        Integer userId = UserUtil.getUserId(principal);
        commentV2Service.mentionUserInComment(requestBody, userId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
