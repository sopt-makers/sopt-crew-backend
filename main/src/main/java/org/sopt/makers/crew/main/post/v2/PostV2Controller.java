package org.sopt.makers.crew.main.post.v2;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.common.util.UserUtil;
import org.sopt.makers.crew.main.post.v2.dto.query.PostGetPostsCommand;
import org.sopt.makers.crew.main.post.v2.dto.request.PostV2CreatePostBodyDto;
import org.sopt.makers.crew.main.post.v2.dto.request.PostV2MentionUserInPostRequestDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostDetailBaseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2CreatePostResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2GetPostsResponseDto;
import org.sopt.makers.crew.main.post.v2.service.PostV2Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/post/v2")
@RequiredArgsConstructor
public class PostV2Controller implements PostV2Api {

    private final PostV2Service postV2Service;

    @Override
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<PostV2CreatePostResponseDto> createPost(
            @Valid @RequestBody PostV2CreatePostBodyDto requestBody, Principal principal) {
        Integer userId = UserUtil.getUserId(principal);
        return ResponseEntity.ok(postV2Service.createPost(requestBody, userId));
    }

    @Override
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PostV2GetPostsResponseDto> getPosts(
            @ModelAttribute @Parameter(hidden = true) PostGetPostsCommand queryCommand,
            Principal principal) {
        Integer userId = UserUtil.getUserId(principal);
        return ResponseEntity.ok(postV2Service.getPosts(queryCommand, userId));
    }

    @Override
    @PostMapping("/mention")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> mentionUserInPost(
            @Valid @RequestBody PostV2MentionUserInPostRequestDto requestBody, Principal principal) {
        Integer userId = UserUtil.getUserId(principal);
        postV2Service.mentionUserInPost(requestBody, userId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    @GetMapping("/{postId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PostDetailBaseDto> getPost(@PathVariable Integer postId, Principal principal) {
        Integer userId = UserUtil.getUserId(principal);
        return ResponseEntity.ok(postV2Service.getPost(userId, postId));
    }
}
