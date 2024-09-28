package org.sopt.makers.crew.main.global.jwt;

import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

// UsernamePasswordAuthenticationToken: 사용자의 인증 정보 저장하고 전달
public class UserAuthentication extends UsernamePasswordAuthenticationToken {

	// 사용자 인증 객체 생성
	public UserAuthentication(Object principal, Object credentials,
		Collection<? extends GrantedAuthority> authorities) {
		super(principal, credentials, authorities);
	}
}
