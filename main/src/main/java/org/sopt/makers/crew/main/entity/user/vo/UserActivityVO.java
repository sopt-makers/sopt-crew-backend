package org.sopt.makers.crew.main.entity.user.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class UserActivityVO {

  private final String part;
  private final int generation;

}