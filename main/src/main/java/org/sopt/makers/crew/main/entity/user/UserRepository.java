package org.sopt.makers.crew.main.entity.user;

import org.sopt.makers.crew.main.common.exception.UnAuthorizedException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

  default User findByIdOrThrow(Integer userId) {
    return findById(userId)
        .orElseThrow(() -> new UnAuthorizedException());
  }
}
