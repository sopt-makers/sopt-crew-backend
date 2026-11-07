package org.sopt.makers.crew.main.entity.user;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.NOT_FOUND_USER;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.UNAUTHORIZED_USER;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.sopt.makers.crew.main.entity.user.projection.UserKeywordsProjection;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.sopt.makers.crew.main.global.exception.UnAuthorizedException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CrewRepository extends JpaRepository<User, Integer> {
	@Query("SELECT u.id FROM User u")
	List<Integer> findAllOrgIds();

	/**
	 * 활동 이력(activities jsonb)에 해당 기수가 있는 회원. 파트 표기는 자유 문자열이라 호출부에서 판정한다.
	 */
	@Query(value = "SELECT * FROM \"user\" u "
		+ "WHERE u.activities @> jsonb_build_array(jsonb_build_object('generation', :generation))",
		nativeQuery = true)
	List<User> findAllByActivityGeneration(@Param("generation") Integer generation);

	@Query("select u from User u where u.id = :userId")
	Optional<UserKeywordsProjection> findInterestedKeywordsByUserId(@Param("userId") Integer userId);

	default User findByIdOrThrow(Integer userId) {
		return findById(userId)
			.orElseThrow(() -> new UnAuthorizedException(UNAUTHORIZED_USER.getErrorCode()));
	}

	default List<User> findAllByIdInOrThrow(List<Integer> userIds) {
		List<User> users = findAllByIdIn(userIds);
		Set<Integer> foundUserIds = users.stream()
			.map(User::getId)
			.collect(Collectors.toSet());

		if (!foundUserIds.containsAll(userIds)) {
			throw new NotFoundException(NOT_FOUND_USER.getErrorCode());
		}

		return users;
	}

	List<User> findAllByIdIn(List<Integer> userIds);
}
