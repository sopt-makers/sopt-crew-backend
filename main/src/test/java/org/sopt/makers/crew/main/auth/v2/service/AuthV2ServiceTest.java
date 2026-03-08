package org.sopt.makers.crew.main.auth.v2.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserFixture;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.external.auth.AuthService;
import org.sopt.makers.crew.main.external.auth.dto.request.AuthUserRequestDto;
import org.sopt.makers.crew.main.external.auth.dto.response.AuthUserActivityResponseDto;
import org.sopt.makers.crew.main.external.auth.dto.response.AuthUserResponseDto;

@ExtendWith(MockitoExtension.class)
class AuthV2ServiceTest {

	@InjectMocks
	private AuthV2ServiceImpl authV2Service;

	@Mock
	private UserRepository userRepository;

	@Mock
	private AuthService authService;

	@Mock
	private AuthV2UserCacheInvalidationService authV2UserCacheInvalidationService;

	@Nested
	class 로그인 {
		@Test
		@DisplayName("기존 유저 정보가 변경되면 cache invalidation service에 위임한다")
		void 기존_유저_정보가_변경되면_cache_invalidation_service에_위임한다() {
			// given
			User user = UserFixture.createStaticUser();
			user.setUserIdForTest(1);

			AuthUserResponseDto responseDto = createAuthUserResponseDto(1, "변경된테스트유저", "010-1111-1111", null);

			doReturn(responseDto).when(authService).getAuthUser(any(AuthUserRequestDto.class));
			doReturn(Optional.of(user)).when(userRepository).findById(1);

			// when
			Integer loginUserId = authV2Service.loginUser(1).userId();

			// then
			assertThat(loginUserId).isEqualTo(1);
			verify(authV2UserCacheInvalidationService).refreshCachesAfterUserUpdate(1);
			verify(authV2UserCacheInvalidationService, never()).refreshCachesAfterUserCreate();
		}

		@Test
		@DisplayName("기존 유저 정보가 변경되지 않으면 cache invalidation service를 호출하지 않는다")
		void 기존_유저_정보가_변경되지_않으면_cache_invalidation_service를_호출하지_않는다() {
			// given
			User user = UserFixture.createStaticUser();
			user.setUserIdForTest(1);

			AuthUserResponseDto responseDto = createAuthUserResponseDto(
				1,
				user.getName(),
				user.getPhone(),
				user.getProfileImage()
			);

			doReturn(responseDto).when(authService).getAuthUser(any(AuthUserRequestDto.class));
			doReturn(Optional.of(user)).when(userRepository).findById(1);

			// when
			Integer loginUserId = authV2Service.loginUser(1).userId();

			// then
			assertThat(loginUserId).isEqualTo(1);
			verifyNoInteractions(authV2UserCacheInvalidationService);
		}

		@Test
		@DisplayName("신규 유저 가입 시 orgId cache 갱신을 위임한다")
		void 신규_유저_가입시_orgId_cache_갱신을_위임한다() {
			// given
			AuthUserResponseDto responseDto = createAuthUserResponseDto(7, "신규테스트유저", "010-7777-7777", "profile-image");
			User savedUser = responseDto.toEntity();

			doReturn(responseDto).when(authService).getAuthUser(any(AuthUserRequestDto.class));
			doReturn(Optional.empty()).when(userRepository).findById(7);
			doReturn(savedUser).when(userRepository).save(any(User.class));

			// when
			Integer loginUserId = authV2Service.loginUser(7).userId();

			// then
			assertThat(loginUserId).isEqualTo(7);
			verify(authV2UserCacheInvalidationService).refreshCachesAfterUserCreate();
			verify(authV2UserCacheInvalidationService, never()).refreshCachesAfterUserUpdate(anyInt());
		}
	}

	private AuthUserResponseDto createAuthUserResponseDto(Integer userId, String name, String phone,
		String profileImage) {
		return new AuthUserResponseDto(
			userId,
			name,
			profileImage,
			"1998-01-01",
			phone,
			"test@sopt.org",
			34,
			List.of(
				new AuthUserActivityResponseDto(1L, 33, "서버", "YB"),
				new AuthUserActivityResponseDto(2L, 34, "서버", "OB")
			)
		);
	}
}
