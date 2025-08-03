package org.sopt.makers.crew.main.entity.user;

import java.util.List;

import org.sopt.makers.crew.main.external.auth.AuthService;
import org.sopt.makers.crew.main.external.auth.dto.request.AuthUserRequestDto;
import org.sopt.makers.crew.main.external.auth.dto.response.AuthUserResponseDto;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PlatformUserRepositoryImpl implements PlatformUserRepository {

	private final AuthService authService;
	private final CrewRepository crewRepository;

	@Override
	public User findByIdOrThrow(Integer userId) {
		AuthUserResponseDto userInfo = authService.getAuthUser(AuthUserRequestDto.from(userId));
		return crewRepository.findByIdOrThrow(userInfo.userId());
	}

	@Override
	public List<User> findAllByIdInOrThrow(List<Integer> userIds) {
		List<Integer> authUserIds = authService.getAuthUsers(AuthUserRequestDto.from(userIds))
			.stream().map(AuthUserResponseDto::userId).toList();
		return crewRepository.findAllByIdInOrThrow(authUserIds);
	}
}
