package org.sopt.makers.crew.main.external;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;

import com.github.benmanes.caffeine.cache.Caffeine;

@TestConfiguration
public class CaffeineCacheConfig {

	@Bean
	public CacheManager cacheManager() {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager();

		cacheManager.setCaffeine(Caffeine.newBuilder()
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.maximumSize(100));

		return cacheManager;
	}
	
}
