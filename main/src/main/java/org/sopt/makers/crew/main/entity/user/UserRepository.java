package org.sopt.makers.crew.main.entity.user;

import java.util.List;
import java.util.Optional;

import org.sopt.makers.crew.main.entity.user.projection.UserKeywordsProjection;

public interface UserRepository {


	Optional<User> findById(Integer userId);

	User save(User user);

	User findByIdOrThrow(Integer userId);

	List<User> findAllByIdIn(List<Integer> userIds);

	List<User> findAll();

	List<User> findAllById(List<Integer> userId);

	List<User> findAllByIdInOrThrow(List<Integer> userIds);

	List<Integer> findAllOrgIds();

	Optional<UserKeywordsProjection> findInterestedKeywordsByUserId(Integer userId);

}
