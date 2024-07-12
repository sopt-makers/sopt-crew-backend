package org.sopt.makers.crew.main.entity.like;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MyLikes {
	private final List<Like> myLikes;

	public boolean isLikeComment(Integer commentId){
		return myLikes.stream()
			.anyMatch(like -> like.getCommentId().equals(commentId));
	}
}
