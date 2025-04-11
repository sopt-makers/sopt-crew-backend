package org.sopt.makers.crew.main.auth.v2.service;

import org.sopt.makers.crew.main.auth.v2.dto.request.AuthV2RequestDto;
import org.sopt.makers.crew.main.auth.v2.dto.response.AuthV2ResponseDto;
import org.sopt.makers.crew.main.entity.meeting.CoLeaderRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.external.playground.PlaygroundService;
import org.sopt.makers.crew.main.external.playground.dto.request.PlaygroundUserRequestDto;
import org.sopt.makers.crew.main.external.playground.dto.response.PlaygroundUserResponseDto;
import org.sopt.makers.crew.main.global.dto.OrgIdListDto;
import org.sopt.makers.crew.main.global.jwt.JwtTokenProvider;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
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
		String token = requestDto.authToken();
		boolean hasToken = token != null && !token.isEmpty();
		log.info("로그인 시도: 토큰 존재 여부={}", hasToken);

		PlaygroundUserResponseDto responseDto = fetchPlaygroundUser(requestDto);
		log.info("Playground API 호출 성공: 토큰 유효함");

		User curUser = userRepository.findByOrgId(responseDto.getId())
			.orElseGet(() -> signUpNewUser(responseDto));
		if (updateUserIfChanged(curUser, responseDto)) {
			clearCacheForUser(curUser.getId());
		}
		String accessToken = jwtTokenProvider.generateAccessToken(curUser.getId(), curUser.getName());
		log.info("로그인 완료: userId={}", curUser.getId());
		return AuthV2ResponseDto.of(accessToken);
	}

	private PlaygroundUserResponseDto fetchPlaygroundUser(AuthV2RequestDto requestDto) {
		try {
			return playgroundService.getUser(PlaygroundUserRequestDto.of(requestDto.authToken()));
		} catch (feign.FeignException.Unauthorized e) {
			log.info("Playground API 호출 실패: 토큰 만료 또는 유효하지 않음");
			throw e;
		}
	}

	private User signUpNewUser(PlaygroundUserResponseDto responseDto) {
		User newUser = responseDto.toEntity();
		User savedUser = userRepository.save(newUser);
		log.info("New user signup: {} {}", savedUser.getId(), savedUser.getName());

		updateOrgIds();

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

		updateOrgIds();

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

	@CachePut(value = "orgIdCache", key = "'allOrgIds'")
	public OrgIdListDto updateOrgIds() {
		OrgIdListDto latestOrgIds = OrgIdListDto.of(userRepository.findAllOrgIds());

		log.info("OrgId cache updated: {}", latestOrgIds);

		return latestOrgIds;
	}

}
