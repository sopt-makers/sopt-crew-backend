package org.sopt.makers.crew.main.post.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.common.util.UserUtil;
import org.sopt.makers.crew.main.post.v2.dto.request.PostV2CreatePostBodyDto;
import org.sopt.makers.crew.main.post.v2.dto.request.PostV2MentionUserInPostRequestDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2CreatePostResponseDto;
import org.sopt.makers.crew.main.post.v2.service.PostV2Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/post/v2")
@RequiredArgsConstructor
@Tag(name = "게시글")
public class PostV2Controller {

    private final PostV2Service postV2Service;

    @Operation(summary = "모임 게시글 작성")
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "성공"),
        @ApiResponse(responseCode = "400", description = "모임이 없습니다.", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한이 없습니다.", content = @Content),
    })
    public ResponseEntity<PostV2CreatePostResponseDto> createPost(
        @Valid @RequestBody PostV2CreatePostBodyDto requestBody, Principal principal) {
        Integer userId = UserUtil.getUserId(principal);
        return ResponseEntity.ok(postV2Service.createPost(requestBody, userId));
    }

    @Operation(summary = "모임 게시글에서 유저 멘션")
    @PostMapping("/mention")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공"),
    })
    public ResponseEntity<Void> mentionUserInPost(
        @Valid @RequestBody PostV2MentionUserInPostRequestDto requestBody, Principal principal) {
        Integer userId = UserUtil.getUserId(principal);
        postV2Service.mentionUserInPost(requestBody, userId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
