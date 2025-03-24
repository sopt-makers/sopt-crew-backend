package org.sopt.makers.crew.main.external.redis;

import org.springframework.context.annotation.Configuration;

@Configuration
//@EnableCaching
public class RedisConfig {

	// @Bean
	// public RedisConnectionFactory redisConnectionFactory(RedisProperties redisProperties) {
	// 	return new LettuceConnectionFactory(redisProperties.getHost(), redisProperties.getPort());
	// }
	//
	// @Bean
	// public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
	// 	RedisTemplate<String, Object> template = new RedisTemplate<>();
	// 	template.setConnectionFactory(redisConnectionFactory);
	//
	// 	ObjectMapper objectMapper = new ObjectMapper();
	// 	objectMapper.registerModule(new JavaTimeModule());
	// 	objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO-8601 형식
	// 	objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(),
	// 		ObjectMapper.DefaultTyping.NON_FINAL); // 타입 정보 추가
	//
	// 	StringRedisSerializer stringSerializer = new StringRedisSerializer();
	// 	GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
	//
	// 	// Key serializer 설정
	// 	template.setKeySerializer(stringSerializer);
	// 	template.setHashKeySerializer(stringSerializer);
	//
	// 	// Value serializer 설정
	// 	template.setValueSerializer(jsonSerializer);
	// 	template.setHashValueSerializer(jsonSerializer);
	//
	// 	template.afterPropertiesSet();
	// 	return template;
	// }
	//
	// @Bean
	// public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
	// 	// 동일한 직렬화 설정 공유
	// 	ObjectMapper objectMapper = new ObjectMapper();
	// 	objectMapper.registerModule(new JavaTimeModule());
	// 	objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO-8601 형식
	// 	objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(),
	// 		ObjectMapper.DefaultTyping.NON_FINAL); // 타입 정보 추가
	//
	// 	GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
	//
	// 	RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
	// 		.entryTtl(Duration.ofHours(24))
	// 		.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
	// 		.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));
	//
	// 	return RedisCacheManager.builder(redisConnectionFactory)
	// 		.cacheDefaults(defaultCacheConfig)
	// 		.build();
	// }
}
