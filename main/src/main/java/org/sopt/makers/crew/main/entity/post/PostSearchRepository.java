package org.sopt.makers.crew.main.entity.post;

import org.sopt.makers.crew.main.post.v2.dto.query.PostGetPostsCommand;
import org.sopt.makers.crew.main.post.v2.dto.response.PostDetailDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostSearchRepository {
    Page<PostDetailDto> findPostList(PostGetPostsCommand queryCommand, Pageable pageable, Integer userId);
}
