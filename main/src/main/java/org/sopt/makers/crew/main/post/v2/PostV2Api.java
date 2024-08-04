package org.sopt.makers.crew.main.post.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import org.sopt.makers.crew.main.post.v2.dto.query.PostGetPostsCommand;
import org.sopt.makers.crew.main.post.v2.dto.request.PostV2CreatePostBodyDto;
import org.sopt.makers.crew.main.post.v2.dto.request.PostV2MentionUserInPostRequestDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostDetailBaseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2CreatePostResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2GetPostCountResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2GetPostsResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "게시글")
public interface PostV2Api {

    @Operation(summary = "모임 게시글 작성")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "성공"),
            @ApiResponse(responseCode = "400", description = "모임이 없습니다.", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다.", content = @Content),
    })
    ResponseEntity<PostV2CreatePostResponseDto> createPost(
            @Valid @RequestBody PostV2CreatePostBodyDto requestBody, Principal principal);

    @Operation(summary = "모임 게시글 목록 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "모임이 없습니다.", content = @Content),
    })
    @Parameters({
            @Parameter(name = "page", description = "페이지, default = 1", example = "1", schema = @Schema(type = "integer", format = "int32")),
            @Parameter(name = "take", description = "가져올 데이터 개수, default = 12", example = "50", schema = @Schema(type = "integer", format = "int32")),
            @Parameter(name = "meetingId", description = "모임 id", example = "0", schema = @Schema(type = "integer", format = "int32"))})
    ResponseEntity<PostV2GetPostsResponseDto> getPosts(
            @ModelAttribute @Parameter(hidden = true) PostGetPostsCommand queryCommand,
            Principal principal);

    @Operation(summary = "게시글에서 멘션하기")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
    })
    ResponseEntity<Void> mentionUserInPost(
            @Valid @RequestBody PostV2MentionUserInPostRequestDto requestBody, Principal principal);

    @Operation(summary = "모임 게시글 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "모임이 없습니다", content = @Content)
    })
    ResponseEntity<PostDetailBaseDto> getPost(@PathVariable Integer postId, Principal principal);

    @Operation(summary = "모임 게시글 개수 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "모임이 없습니다", content = @Content)
    })
    ResponseEntity<PostV2GetPostCountResponseDto> getPostCount(@RequestParam Integer meetingId);

    @Operation(summary = "모임 게시글 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "모임이 없습니다.", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다.", content = @Content)
    })
    ResponseEntity<Void> deletePost(@PathVariable Integer postId, Principal principal);
}
