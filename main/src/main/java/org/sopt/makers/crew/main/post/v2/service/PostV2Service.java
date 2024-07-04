package org.sopt.makers.crew.main.post.v2.service;

import org.sopt.makers.crew.main.post.v2.dto.request.PostV2CreatePostBodyDto;
import org.sopt.makers.crew.main.post.v2.dto.request.PostV2MentionUserInPostRequestDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2CreatePostResponseDto;

public interface PostV2Service {

    PostV2CreatePostResponseDto createPost(PostV2CreatePostBodyDto requestBody, Integer userId);

    void mentionUserInPost(PostV2MentionUserInPostRequestDto requestBody, Integer userId);
}
