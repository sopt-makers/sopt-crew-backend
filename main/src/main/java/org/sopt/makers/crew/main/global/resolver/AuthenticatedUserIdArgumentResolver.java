package org.sopt.makers.crew.main.global.resolver;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import org.sopt.makers.crew.main.global.annotation.AuthenticatedUserId;
import org.sopt.makers.crew.main.global.exception.UnAuthorizedException;
import org.sopt.makers.crew.main.global.security.authentication.MakersAuthentication;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthenticatedUserIdArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(AuthenticatedUserId.class) && parameter.getParameterType()
			.equals(Integer.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()
			|| !(authentication instanceof MakersAuthentication makersAuth)) {
			throw new UnAuthorizedException(UNAUTHORIZED_INVALID_AUTH.getErrorCode());
		}

		return Integer.valueOf(makersAuth.getUserId());
	}
}
