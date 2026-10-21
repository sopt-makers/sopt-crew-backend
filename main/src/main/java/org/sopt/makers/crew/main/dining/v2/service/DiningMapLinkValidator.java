package org.sopt.makers.crew.main.dining.v2.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 회차 장소 링크 검증. 네이버지도 1개, 카카오맵 1개까지 허용하며 단축 링크도 허용한다.
 */
@Component
public class DiningMapLinkValidator {

	private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
	private static final Set<String> NAVER_MAP_HOSTS = Set.of("map.naver.com", "naver.me");
	private static final Set<String> KAKAO_MAP_HOSTS = Set.of("map.kakao.com", "place.map.kakao.com", "kko.to");

	public void validate(String naverMapLink, String kakaoMapLink) {
		validateHost(naverMapLink, NAVER_MAP_HOSTS);
		validateHost(kakaoMapLink, KAKAO_MAP_HOSTS);
	}

	private void validateHost(String link, Set<String> allowedHosts) {
		if (!StringUtils.hasText(link)) {
			return;
		}

		try {
			URI uri = new URI(link.trim());
			String scheme = uri.getScheme();
			String host = uri.getHost();

			if (scheme == null || host == null
				|| !ALLOWED_SCHEMES.contains(scheme.toLowerCase(Locale.ROOT))
				|| !allowedHosts.contains(host.toLowerCase(Locale.ROOT))) {
				throw new BadRequestException(INVALID_DINING_MAP_LINK.getErrorCode());
			}
		} catch (URISyntaxException e) {
			throw new BadRequestException(INVALID_DINING_MAP_LINK.getErrorCode());
		}
	}
}
