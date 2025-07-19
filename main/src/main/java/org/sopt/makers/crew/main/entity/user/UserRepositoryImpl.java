package org.sopt.makers.crew.main.entity.user;

import java.util.List;
import java.util.Optional;

import org.sopt.makers.crew.main.entity.user.projection.UserKeywordsProjection;
import org.sopt.makers.crew.main.global.exception.ServerException;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

	private final CrewRepository crewRepository; // jpa 쓸 거면 여기랑 같이
	private final PlatformUserRepository platformUserRepository; // 여기는 platform 쏘는 로직만

	@Override
	public Optional<User> findById(Integer userId) {
		return crewRepository.findById(userId);
	}

	@Override
	public User save(User user) {
		return crewRepository.save(user);
	}

	@Override
	public User findByIdOrThrow(Integer userId) {
		try {
			return platformUserRepository.findByIdOrThrow(userId);
		} catch (ServerException e) {
			return crewRepository.findByIdOrThrow(userId);
		}
	}

	@Override
	public List<User> findAllByIdIn(List<Integer> userIds) {
		try {
			return platformUserRepository.findAllByIdIn(userIds);
		} catch (ServerException e) {
			return crewRepository.findAllByIdIn(userIds);
		}
	}

	@Override
	public List<User> findAll() {
		return crewRepository.findAll();
	}

	@Override
	public List<User> findAllByIdInOrThrow(List<Integer> userIds) {
		try {
			return platformUserRepository.findAllByIdInOrThrow(userIds);
		} catch (ServerException e) {
			return crewRepository.findAllByIdInOrThrow(userIds);
		}
	}

	@Override
	public List<Integer> findAllOrgIds() {
		return crewRepository.findAllOrgIds();
	}

	@Override
	public List<User> findAllById(List<Integer> userIds) {
		return crewRepository.findAllById(userIds);
	}

	@Override
	public Optional<UserKeywordsProjection> findInterestedKeywordsByUserId(Integer userId) {
		return crewRepository.findInterestedKeywordsByUserId(userId);
	}

}
