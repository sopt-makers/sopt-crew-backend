package org.sopt.makers.crew.main.entity.user;

import java.util.Optional;
import org.sopt.makers.crew.main.common.exception.UnAuthorizedException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

  Optional<User> findByOrgId(Integer orgId);

  default User findByIdOrThrow(Integer userId) {
    return findById(userId).orElseThrow(() -> new UnAuthorizedException());
  }

  default User findByOrgIdOrThrow(Integer orgUserId) {
    return findByOrgId(orgUserId).orElseThrow(() -> new UnAuthorizedException());
  }
}
