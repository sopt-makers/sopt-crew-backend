package org.sopt.makers.crew.main.post.v2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class PostV2CreatePostResponseDto {

  /**
   * 생성된 게시물 id
   */
  private Integer postId;

}
