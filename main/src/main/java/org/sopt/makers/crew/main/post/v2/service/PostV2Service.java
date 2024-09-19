package org.sopt.makers.crew.main.post.v2.service;

import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.post.v2.dto.query.PostGetPostsCommand;
import org.sopt.makers.crew.main.post.v2.dto.request.PostV2CreatePostBodyDto;
import org.sopt.makers.crew.main.post.v2.dto.request.PostV2MentionUserInPostRequestDto;
import org.sopt.makers.crew.main.post.v2.dto.request.PostV2UpdatePostBodyDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostDetailBaseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2CreatePostResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2GetPostCountResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2GetPostsResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2ReportResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2SwitchPostLikeResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2UpdatePostResponseDto;

public interface PostV2Service {

	PostV2CreatePostResponseDto createPost(PostV2CreatePostBodyDto requestBody, Integer userId);

	PostV2GetPostsResponseDto getPosts(PostGetPostsCommand queryCommand, User user);

	PostDetailBaseDto getPost(Integer userId, Integer postId);

	void mentionUserInPost(PostV2MentionUserInPostRequestDto requestBody, Integer userId);

	PostV2GetPostCountResponseDto getPostCount(Integer meetingId);

	void deletePost(Integer postId, Integer userId);

	PostV2UpdatePostResponseDto updatePost(Integer postId, PostV2UpdatePostBodyDto requestBody, Integer userId);

	PostV2ReportResponseDto reportPost(Integer postId, Integer userId);

	PostV2SwitchPostLikeResponseDto switchPostLike(Integer postId, Integer userId);
}
