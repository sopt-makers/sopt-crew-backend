package org.sopt.makers.crew.main.auth.v2.service;

import org.sopt.makers.crew.main.auth.v2.dto.response.AuthV2ResponseDto;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.external.auth.AuthService;
import org.sopt.makers.crew.main.external.auth.dto.request.AuthUserRequestDto;
import org.sopt.makers.crew.main.external.auth.dto.response.AuthUserResponseDto;
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
	private final AuthService authService;
	private final AuthV2UserCacheInvalidationService authV2UserCacheInvalidationService;

	@Override
	@Transactional
	public AuthV2ResponseDto loginUser(Integer userId) {
		AuthUserResponseDto responseDto = fetchAuthUser(userId);
		log.info("{} - Auth Server API 호출 성공: 토큰 유효함", userId);

		User curUser = userRepository.findById(responseDto.userId())
			.orElseGet(() -> signUpNewUser(responseDto));

		if (updateUserIfChanged(curUser, responseDto)) {
			authV2UserCacheInvalidationService.refreshCachesAfterUserUpdate(curUser.getId());
		}

		log.info("로그인 완료: userId={}", curUser.getId());
		return AuthV2ResponseDto.from(curUser.getId());
	}

	private AuthUserResponseDto fetchAuthUser(Integer userId) {
		try {
			return authService.getAuthUser(AuthUserRequestDto.from(userId));
		} catch (feign.FeignException.Unauthorized e) {
			log.info("Playground API 호출 실패: 토큰 만료 또는 유효하지 않음");
			throw e;
		}
	}

	private User signUpNewUser(AuthUserResponseDto responseDto) {
		User newUser = responseDto.toEntity();
		User savedUser = userRepository.save(newUser);
		log.info("New user signup: {} {}", savedUser.getId(), savedUser.getName());

		authV2UserCacheInvalidationService.refreshCachesAfterUserCreate();

		return savedUser;
	}

	private boolean updateUserIfChanged(User curUser, AuthUserResponseDto responseDto) {
		User authUser = responseDto.toEntity();
		boolean isUpdated = curUser.updateIfChanged(authUser);

		if (isUpdated) {
			log.info("User updated: {}", curUser.getId());
		}

		return isUpdated;
	}

}
