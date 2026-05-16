package org.sopt.makers.crew.main.external.redis;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

class RedisConfigConditionTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withUserConfiguration(RedisConfig.class);

	@Test
	void cacheType이_redis이면_RedisConfig가_활성화된다() {
		contextRunner
			.withPropertyValues(
				"spring.cache.type=redis",
				"spring.data.redis.host=localhost",
				"spring.data.redis.port=6379"
			)
			.run(context -> {
				assertThat(context).hasSingleBean(RedisConnectionFactory.class);
				assertThat(context).hasSingleBean(RedisTemplate.class);
				assertThat(context).hasSingleBean(RedisCacheManager.class);
			});
	}

	@Test
	void cacheType이_caffeine이면_RedisConfig가_비활성화된다() {
		contextRunner
			.withPropertyValues("spring.cache.type=caffeine")
			.run(context -> {
				assertThat(context).doesNotHaveBean(RedisConnectionFactory.class);
				assertThat(context).doesNotHaveBean(RedisTemplate.class);
				assertThat(context).doesNotHaveBean(RedisCacheManager.class);
			});
	}
}
