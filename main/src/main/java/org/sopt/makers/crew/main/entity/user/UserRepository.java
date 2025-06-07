package org.sopt.makers.crew.main.entity.user;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.List;
import java.util.Optional;

import org.sopt.makers.crew.main.entity.user.projection.UserKeywrodsProjection;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.sopt.makers.crew.main.global.exception.UnAuthorizedException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {

	Optional<User> findByOrgId(Integer orgId);

	default User findByIdOrThrow(Integer userId) {
		return findById(userId)
			.orElseThrow(() -> new UnAuthorizedException(UNAUTHORIZED_USER.getErrorCode()));
	}

	List<User> findAllByIdIn(List<Integer> userIds);

	default List<User> findAllByIdInOrThrow(List<Integer> userIds) {
		List<User> users = findAllByIdIn(userIds);
		List<Integer> foundUserIds = users.stream()
			.map(User::getId)
			.toList();

		if (!foundUserIds.containsAll(userIds)) {
			throw new NotFoundException(NOT_FOUND_USER.getErrorCode());
		}

		return users;
	}

	@Query("SELECT u.orgId FROM User u")
	List<Integer> findAllOrgIds();

	@Query("select u from User u where u.id = :userId")
	Optional<UserKeywrodsProjection> findInterestedKeywordsByUserId(@Param("userId") Integer userId);
}
