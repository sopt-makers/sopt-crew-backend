package org.sopt.makers.crew.main.external.caffeine;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@Endpoint(id = "cachecontents")
@RequiredArgsConstructor
public class CachesEndpointExtension {

	private final CacheManager cacheManager;

	@ReadOperation
	public Map<String, Map<String, Object>> cachesWithValues() {
		Map<String, Map<String, Object>> result = new HashMap<>();

		cacheManager.getCacheNames().forEach(cacheName -> {
			CaffeineCache cache = (CaffeineCache)cacheManager.getCache(cacheName);
			com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = cache.getNativeCache();

			Map<String, Object> cacheContents = new HashMap<>();
			nativeCache.asMap().forEach((key, value) ->
				cacheContents.put(key.toString(), value));

			result.put(cacheName, cacheContents);
		});

		return result;
	}

}
