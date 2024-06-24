package org.sopt.makers.crew.main.post.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import org.sopt.makers.crew.main.post.v2.dto.query.PostGetPostsCommand;
import org.sopt.makers.crew.main.post.v2.dto.request.PostV2CreatePostBodyDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2CreatePostResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2GetPostsResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;

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
    ResponseEntity<PostV2GetPostsResponseDto> getPosts(
            @ModelAttribute PostGetPostsCommand queryCommand, Principal principal);
}
