package org.sopt.makers.crew.main.external.caffeine;

import java.util.Map;

import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Endpoint(id = "cachecontents")
@Slf4j
@RequiredArgsConstructor
public class CachesEndpointExtension {

	private static final String CACHE_LIST_SELECTOR = "caches";

	private final CacheQueryService cacheQueryService;

	@ReadOperation
	public Map<String, Map<String, Object>> cachesWithValues() {
		return cacheQueryService.getAllCacheContents();

	}

	@ReadOperation
	public Object handleSingleSelector(@Selector String selector) {
		if (CACHE_LIST_SELECTOR.equals(selector)) {
			return cacheQueryService.getCacheList();
		}
		return cacheQueryService.getCacheKeys(selector);
	}

	@ReadOperation
	public Object retrieveCacheInfo(@Selector String cacheName, @Selector Integer key) {
		return cacheQueryService.getCacheValue(cacheName, key)
			.orElse(null);
	}

	@DeleteOperation
	public void clearAllCaches() {
		cacheQueryService.clearAllCache();
	}

	@DeleteOperation
	public void clearCache(@Selector String cacheName) {
		cacheQueryService.clearCache(cacheName);
	}

	@DeleteOperation
	public void evictCache(@Selector String cacheName, @Selector Integer key) {
		cacheQueryService.evictCache(cacheName, key);
	}

}
