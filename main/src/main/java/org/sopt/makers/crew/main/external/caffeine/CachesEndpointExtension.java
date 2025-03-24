package org.sopt.makers.crew.main.external.caffeine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Endpoint(id = "cachecontents")
@Slf4j
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
		log.info("result: {}", result.get("meetingCache"));

		return result;
	}

	@ReadOperation
	public Object handleSingleSelector(@Selector String selector) {
		if ("caches".equals(selector)) {
			List<CacheInfo> cacheInfos = new ArrayList<>();
			cacheManager.getCacheNames().forEach(cacheName -> {
				Cache cache = cacheManager.getCache(cacheName);
				if (cache instanceof CaffeineCache) {
					CaffeineCache caffeineCache = (CaffeineCache)cache;
					com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();

					cacheInfos.add(new CacheInfo(
						cacheName,
						nativeCache.estimatedSize(),
						"Caffeine"
					));
				}
			});
			return cacheInfos;
		} else {
			// 특정 캐시의 키 목록 조회
			CaffeineCache cache = (CaffeineCache)cacheManager.getCache(selector); // key
			if (Objects.isNull(cache)) {
				return null;
			}

			com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = cache.getNativeCache();

			log.info("cacheName: {}", cache);
			log.info("nativeCache: {}", nativeCache.asMap().get(90).toString());
			return nativeCache.asMap().keySet()
				.stream()
				.map(Object::toString)
				.toList();
		}
	}

	@ReadOperation
	public Object retrieveCacheInfo(@Selector String cacheName, @Selector Integer key) {

		CaffeineCache cache = (CaffeineCache)cacheManager.getCache(cacheName);// key

		com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = cache.getNativeCache();

		return nativeCache.asMap().get(key);
	}
}



