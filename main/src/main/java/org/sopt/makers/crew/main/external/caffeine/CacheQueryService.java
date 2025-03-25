package org.sopt.makers.crew.main.external.caffeine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheQueryService {
	private final CacheManager cacheManager;

	public List<CacheInfo> getCacheList() {
		List<CacheInfo> cacheInfos = new ArrayList<>();
		cacheManager.getCacheNames().forEach(cacheName -> {
			Cache cache = cacheManager.getCache(cacheName);
			if (cache instanceof CaffeineCache caffeineCache) {
				com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
				cacheInfos.add(new CacheInfo(
					cacheName,
					nativeCache.estimatedSize(),
					"Caffeine"
				));
			}
		});
		return cacheInfos;
	}

	public List<String> getCacheKeys(String cacheName) {
		CaffeineCache cache = (CaffeineCache)cacheManager.getCache(cacheName);
		if (Objects.isNull(cache)) {
			return Collections.emptyList();
		}

		com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = cache.getNativeCache();
		return nativeCache.asMap().keySet()
			.stream()
			.map(Object::toString)
			.toList();
	}

	public Map<String, Map<String, Object>> getAllCacheContents() {
		Map<String, Map<String, Object>> result = new HashMap<>();

		cacheManager.getCacheNames().forEach(cacheName -> {
			CaffeineCache cache = (CaffeineCache)cacheManager.getCache(cacheName);
			com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = Objects.requireNonNull(cache)
				.getNativeCache();

			Map<String, Object> cacheContents = new HashMap<>();
			nativeCache.asMap().forEach((key, value) ->
				cacheContents.put(key.toString(), value));

			result.put(cacheName, cacheContents);
		});

		return result;
	}

	public Optional<Object> getCacheValue(String cacheName, Integer key) {
		return getNativeCache(cacheName)
			.map(nativeCache -> nativeCache.asMap().get(key));
	}

	private Optional<com.github.benmanes.caffeine.cache.Cache<Object, Object>> getNativeCache(String cacheName) {
		Cache cache = cacheManager.getCache(cacheName);
		if (cache instanceof CaffeineCache caffeineCache) {
			return Optional.of(caffeineCache.getNativeCache());
		}
		return Optional.empty();
	}
}
