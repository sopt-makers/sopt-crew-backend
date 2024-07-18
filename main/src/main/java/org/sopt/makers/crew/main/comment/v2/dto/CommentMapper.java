package org.sopt.makers.crew.main.comment.v2.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.sopt.makers.crew.main.comment.v2.dto.request.CommentV2CreateCommentBodyDto;
import org.sopt.makers.crew.main.entity.comment.Comment;
import org.sopt.makers.crew.main.entity.post.Post;
import org.sopt.makers.crew.main.entity.user.User;

@Mapper(componentModel = "spring")
public interface CommentMapper {
	@Mapping(source = "post", target = "post")
	@Mapping(source = "requestBody.contents", target = "contents")
	@Mapping(source = "user", target = "user")
	@Mapping(source = "user.id", target = "userId")
	Comment toComment(CommentV2CreateCommentBodyDto requestBody, Post post, User user, int depth, int order,
		Integer parentId);
}
