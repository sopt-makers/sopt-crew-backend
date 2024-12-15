package org.sopt.makers.crew.main.auth.v2.service;

import org.sopt.makers.crew.main.auth.v2.dto.request.AuthV2RequestDto;
import org.sopt.makers.crew.main.auth.v2.dto.response.AuthV2ResponseDto;
import org.sopt.makers.crew.main.entity.meeting.CoLeaderRepository;
import org.sopt.makers.crew.main.global.jwt.JwtTokenProvider;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.external.playground.PlaygroundService;
import org.sopt.makers.crew.main.external.playground.dto.request.PlaygroundUserRequestDto;
import org.sopt.makers.crew.main.external.playground.dto.response.PlaygroundUserResponseDto;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthV2ServiceImpl implements AuthV2Service {

	private final UserRepository userRepository;
	private final CoLeaderRepository coLeaderRepository;

	private final PlaygroundService playgroundService;
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	@Transactional
	public AuthV2ResponseDto loginUser(AuthV2RequestDto requestDto) {
		PlaygroundUserResponseDto responseDto = fetchPlaygroundUser(requestDto);
		User curUser = userRepository.findByOrgId(responseDto.getId())
			.orElseGet(() -> signUpNewUser(responseDto));

		if (updateUserIfChanged(curUser, responseDto)) {
			clearCacheForUser(curUser.getId());
		}

		String accessToken = jwtTokenProvider.generateAccessToken(curUser.getId(), curUser.getName());
		log.info("Access token generated for user {}: {}", curUser.getId(), accessToken);
		return AuthV2ResponseDto.of(accessToken);
	}

	private PlaygroundUserResponseDto fetchPlaygroundUser(AuthV2RequestDto requestDto) {
		return playgroundService.getUser(PlaygroundUserRequestDto.of(requestDto.authToken()));
	}

	private User signUpNewUser(PlaygroundUserResponseDto responseDto) {
		User newUser = responseDto.toEntity();
		User savedUser = userRepository.save(newUser);
		log.info("New user signup: {} {}", savedUser.getId(), savedUser.getName());
		return savedUser;
	}

	private boolean updateUserIfChanged(User curUser, PlaygroundUserResponseDto responseDto) {
		User playgroundUser = responseDto.toEntity();
		boolean isUpdated = curUser.updateIfChanged(playgroundUser);

		if (isUpdated) {
			log.info("User updated: {}", curUser.getId());
		}

		return isUpdated;
	}

	private void clearCacheForUser(Integer userId) {
		clearCacheForLeader(userId);

		coLeaderRepository.findAllByUserIdWithMeeting(userId).forEach(
			coLeader -> clearCacheForCoLeader(coLeader.getMeeting().getId())
		);
		log.info("Cache cleared for user: {}", userId);
	}

	@Caching(evict = {
		@CacheEvict(value = "meetingLeaderCache", key = "#userId")
	})
	public void clearCacheForLeader(Integer userId) {

	}

	@Caching(evict = {
		@CacheEvict(value = "coLeadersCache", key = "#meetingId")
	})
	public void clearCacheForCoLeader(Integer meetingId) {

	}
}
