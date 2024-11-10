package org.sopt.makers.crew.main.entity.user;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional
public class UserReader {
	private final UserRepository userRepository;

	@Cacheable(value = "meetingLeaderCache", key = "#userId")
	public User getMeetingLeader(Integer userId) {
		return userRepository.findByIdOrThrow(userId);
	}
}
