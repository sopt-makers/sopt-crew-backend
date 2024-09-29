package org.sopt.makers.crew.main.entity.user;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.Optional;

import org.sopt.makers.crew.main.global.exception.UnAuthorizedException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

	Optional<User> findByOrgId(Integer orgId);

	default User findByIdOrThrow(Integer userId) {
		return findById(userId)
			.orElseThrow(() -> new UnAuthorizedException(UNAUTHORIZED_USER.getErrorCode()));
	}

}