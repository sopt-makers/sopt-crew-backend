package org.sopt.makers.crew.main.external.redis;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.external.redisContainerBaseTest;
import org.sopt.makers.crew.main.global.config.CacheConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {CacheConfig.class, RedisConfig.class})
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.cache.type=redis")
class RedisConfigIntegrationTest extends redisContainerBaseTest {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private RedisCacheManager redisCacheManager;

	@Test
	void redisTemplate이_실제_Redis에_값을_저장하고_조회한다() {
		String key = "redis-config-integration:string";

		redisTemplate.opsForValue().set(key, "crew");

		assertThat(redisTemplate.opsForValue().get(key)).isEqualTo("crew");
	}

	@Test
	void redisTemplate이_json_직렬화된_객체를_저장하고_조회한다() {
		String key = "redis-config-integration:object";
		RedisSerializableValue value = new RedisSerializableValue("crew", 1);

		redisTemplate.opsForValue().set(key, value);

		assertThat(redisTemplate.opsForValue().get(key)).isEqualTo(value);
	}

	@Test
	void redisCacheManager가_Redis_캐시를_사용한다() {
		redisCacheManager.getCache("redisIntegrationCache").put("key", "value");

		assertThat(redisCacheManager.getCache("redisIntegrationCache").get("key", String.class))
			.isEqualTo("value");
	}

	public static class RedisSerializableValue {
		private String service;
		private int count;

		public RedisSerializableValue() {
		}

		public RedisSerializableValue(String service, int count) {
			this.service = service;
			this.count = count;
		}

		public String getService() {
			return service;
		}

		public void setService(String service) {
			this.service = service;
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof RedisSerializableValue that)) {
				return false;
			}
			return count == that.count && Objects.equals(service, that.service);
		}

		@Override
		public int hashCode() {
			return Objects.hash(service, count);
		}
	}
}
