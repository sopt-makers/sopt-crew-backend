package org.sopt.makers.crew.main.auth.v2;

import org.sopt.makers.crew.main.auth.v2.dto.response.AuthV2ResponseDto;
import org.sopt.makers.crew.main.auth.v2.service.AuthV2Service;
import org.sopt.makers.crew.main.global.annotation.AuthenticatedUserId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth/v2")
@RequiredArgsConstructor
public class AuthV2Controller implements AuthV2Api {

	private final AuthV2Service authV2Service;

	@Override
	@PostMapping
	public ResponseEntity<AuthV2ResponseDto> loginUser(
		@AuthenticatedUserId Integer userId
	) {
		AuthV2ResponseDto responseDto = authV2Service.loginUser(userId);

		return ResponseEntity.ok(responseDto);
	}
}
