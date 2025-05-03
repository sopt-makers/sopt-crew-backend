package org.sopt.makers.crew.main.external.caffeine;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.sopt.makers.crew.main.global.exception.NotFoundException;
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
	private static final String CACHE_TYPE_CAFFEINE = "Caffeine";
	private static final String LOG_CLEAR_CACHE = "clear Cache {}";
	private static final String LOG_CLEAR_ALL_CACHES = "Clearing all caches now time : {}";
	private static final String LOG_EVICT_CACHE = "Evicting Cache {}";
	private static final String NOT_FOUND_CACHE = "not found cache";

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
					CACHE_TYPE_CAFFEINE
				));
			}
		});
		return cacheInfos;
	}

	public List<String> getCacheKeys(String cacheName) {
		return getNativeCache(cacheName)
			.map(nativeCache -> nativeCache.asMap().keySet()
				.stream()
				.map(Object::toString)
				.toList())
			.orElse(Collections.emptyList());
	}

	public Map<String, Map<String, Object>> getAllCacheContents() {
		Map<String, Map<String, Object>> result = new HashMap<>();

		cacheManager.getCacheNames().forEach(cacheName -> {
			getNativeCache(cacheName).ifPresent(nativeCache -> {
				Map<String, Object> cacheContents = new HashMap<>();
				nativeCache.asMap().forEach((key, value) ->
					cacheContents.put(key.toString(), value));

				result.put(cacheName, cacheContents);
			});
		});

		return result;
	}

	public Optional<Object> getCacheValue(String cacheName, Integer key) {
		return getNativeCache(cacheName)
			.map(nativeCache -> nativeCache.asMap().get(key));
	}

	private Cache getRequiredCache(String cacheName) {
		Cache cache = cacheManager.getCache(cacheName);
		if (Objects.isNull(cache))
			throw new NotFoundException(NOT_FOUND_CACHE);
		return cache;
	}

	public void clearCache(String cacheName) {
		log.info(LOG_CLEAR_CACHE, cacheName);
		getRequiredCache(cacheName).clear();
	}

	public void clearAllCache() {
		log.info(LOG_CLEAR_ALL_CACHES, LocalDateTime.now());
		cacheManager.getCacheNames().forEach(cacheName ->
			getRequiredCache(cacheName).clear());
	}

	public void evictCache(String cacheName, Integer key) {
		log.info(LOG_EVICT_CACHE, cacheName);
		if (getCacheElementCount(cacheName) <= 0) {
			throw new NotFoundException(NOT_FOUND_CACHE);
		}
		if (!getRequiredCache(cacheName).evictIfPresent(key))
			throw new NotFoundException(NOT_FOUND_CACHE);
	}

	private Optional<com.github.benmanes.caffeine.cache.Cache<Object, Object>> getNativeCache(String cacheName) {
		Cache cache = cacheManager.getCache(cacheName);
		if (cache instanceof CaffeineCache caffeineCache) {
			return Optional.of(caffeineCache.getNativeCache());
		}
		return Optional.empty();
	}

	private long getCacheElementCount(String cacheName) {
		return cacheManager.getCacheNames()
			.stream().filter(name -> name.equals(cacheName))
			.count();
	}

}
