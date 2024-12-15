package org.sopt.makers.crew.main.entity.user;

import org.sopt.makers.crew.main.global.dto.MeetingCreatorDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserReader {
	private final UserRepository userRepository;

	@Cacheable(value = "meetingLeaderCache", key = "#userId")
	public MeetingCreatorDto getMeetingLeader(Integer userId) {
		return MeetingCreatorDto.of(userRepository.findByIdOrThrow(userId));
	}
}
