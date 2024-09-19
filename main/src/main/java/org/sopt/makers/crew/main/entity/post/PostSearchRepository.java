package org.sopt.makers.crew.main.entity.post;

import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.post.v2.dto.query.PostGetPostsCommand;
import org.sopt.makers.crew.main.post.v2.dto.response.PostDetailBaseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostDetailResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostSearchRepository {
	Page<PostDetailResponseDto> findPostList(PostGetPostsCommand queryCommand, Pageable pageable, User user);

	PostDetailBaseDto findPost(Integer userId, Integer postId);
}
