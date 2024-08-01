package org.sopt.makers.crew.main.post.v2.service;

import org.sopt.makers.crew.main.post.v2.dto.query.PostGetPostsCommand;
import org.sopt.makers.crew.main.post.v2.dto.request.PostV2CreatePostBodyDto;
import org.sopt.makers.crew.main.post.v2.dto.request.PostV2MentionUserInPostRequestDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostDetailBaseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2CreatePostResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2GetPostCountResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2GetPostsResponseDto;

public interface PostV2Service {

    PostV2CreatePostResponseDto createPost(PostV2CreatePostBodyDto requestBody, Integer userId);

    PostV2GetPostsResponseDto getPosts(PostGetPostsCommand queryCommand, Integer userId);

    PostDetailBaseDto getPost(Integer userId, Integer postId);

    void mentionUserInPost(PostV2MentionUserInPostRequestDto requestBody, Integer userId);

    PostV2GetPostCountResponseDto getPostCount(Integer meetingId);

    void deletePost(Integer postId, Integer userId);
}
