package org.sopt.makers.crew.main.common.util;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.common.exception.UnAuthorizedException;

@RequiredArgsConstructor
public class UserUtil {

  public static Integer getUserId(Principal principal) {
    if (principal == null) {
      throw new UnAuthorizedException();
    }
    return Integer.valueOf(principal.getName());
  }
}
