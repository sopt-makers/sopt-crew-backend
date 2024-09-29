package org.sopt.makers.crew.main.auth.v2.service;

import java.util.Optional;

import org.sopt.makers.crew.main.auth.v2.dto.request.AuthV2RequestDto;
import org.sopt.makers.crew.main.auth.v2.dto.response.AuthV2ResponseDto;
import org.sopt.makers.crew.main.global.jwt.JwtTokenProvider;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.external.playground.PlaygroundService;
import org.sopt.makers.crew.main.external.playground.dto.request.PlaygroundUserRequestDto;
import org.sopt.makers.crew.main.external.playground.dto.response.PlaygroundUserResponseDto;
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

	private final PlaygroundService playgroundService;
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	@Transactional
	public AuthV2ResponseDto loginUser(AuthV2RequestDto requestDto) {

		// 플그 서버로의 요청
		PlaygroundUserResponseDto responseDto = playgroundService.getUser(PlaygroundUserRequestDto.of(requestDto.authToken()));
		Optional<User> user = userRepository.findByOrgId(responseDto.getId());

		/**
		 * @note: 회원가입 경우
		 *
		 * */
		if (user.isEmpty()) {
			User newUser = responseDto.toEntity();
			userRepository.save(newUser);

			log.info("new user signup : {} {}", newUser.getId(), newUser.getName());
			String accessToken = jwtTokenProvider.generateAccessToken(newUser.getId(), newUser.getName());
			return AuthV2ResponseDto.of(accessToken);
		}

		/**
		 * @note: 로그인 경우 : 기존 정보에서 변화있는 부분은 업데이트 한다.
		 *
		 * */
		User curUser = user.get();
		curUser.updateUser(responseDto.getName(), responseDto.getId(), responseDto.getUserActivities(),
			responseDto.getProfileImage(), responseDto.getPhone());

		String accessToken = jwtTokenProvider.generateAccessToken(curUser.getId(), curUser.getName());
		log.info("accessToken : {}", accessToken);

		return AuthV2ResponseDto.of(accessToken);
	}
}
