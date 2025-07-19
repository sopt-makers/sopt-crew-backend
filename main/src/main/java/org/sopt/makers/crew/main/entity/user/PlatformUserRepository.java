package org.sopt.makers.crew.main.entity.user;

import java.util.List;

public interface PlatformUserRepository {

	User findByIdOrThrow(Integer userId);

	List<User> findAllByIdIn(List<Integer> userIds);

	List<User> findAllByIdInOrThrow(List<Integer> userIds);
}
