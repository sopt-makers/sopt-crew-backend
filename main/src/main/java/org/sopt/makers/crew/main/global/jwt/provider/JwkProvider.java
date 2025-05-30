package org.sopt.makers.crew.main.global.jwt.provider;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.io.IOException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Duration;

import org.sopt.makers.crew.main.external.auth.AuthClient;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.ServerException;
import org.sopt.makers.crew.main.global.exception.UnAuthorizedException;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwkProvider {
	private final Cache<String, PublicKey> keyCache;
	private final AuthClient authClient;

	public JwkProvider(AuthClient authClient) {
		this.keyCache = Caffeine.newBuilder()
			.maximumSize(20)
			.expireAfterWrite(Duration.ofDays(1))
			.build();
		this.authClient = authClient;
	}

	/**
	 * 주어진 kid에 해당하는 PublicKey 반환
	 * @param kid JWT header의 Key ID
	 * @return RSA PublicKey
	 */
	public PublicKey getPublicKey(String kid) {
		return keyCache.get(kid, this::resolvePublicKey);
	}

	private PublicKey resolvePublicKey(String kid) {
		try {
			JWKSet jwkSet = loadJwkSet();
			JWK jwk = findJwkByKeyId(jwkSet, kid);
			return convertToPublicKey(jwk);
		} catch (UnAuthorizedException | BadRequestException e) {
			throw e;
		} catch (RuntimeException | IOException | ParseException e) {
			log.error("Failed to resolve public key for kid={}: {}", kid, e.getMessage());
			throw new ServerException(JWK_FETCH_FAILED.getErrorCode());
		}
	}

	private JWKSet loadJwkSet() throws IOException, ParseException {
		String json = authClient.getJwk();
		return JWKSet.parse(json);
	}

	private JWK findJwkByKeyId(JWKSet jwkSet, String kid) {
		return jwkSet.getKeys().stream()
			.filter(jwk -> kid.equals(jwk.getKeyID()))
			.findFirst()
			.orElseThrow(() -> new UnAuthorizedException(UNAUTHORIZED_INVALID_KID.getErrorCode()));
	}

	private RSAPublicKey convertToPublicKey(JWK jwk) {
		if (!(jwk instanceof RSAKey rsaKey)) {
			throw new BadRequestException(JWK_INVALID_FORMAT.getErrorCode());
		}

		try {
			return rsaKey.toRSAPublicKey();
		} catch (JOSEException e) {
			throw new BadRequestException(JWK_INVALID_FORMAT.getErrorCode());
		}
	}

	/**
	 * 캐시 무효화 (예: 복호화 실패 시)
	 */
	public void invalidateKey(String kid) {
		log.warn("Invalidating cached JWK for kid={}", kid);
		keyCache.invalidate(kid);
	}
}
