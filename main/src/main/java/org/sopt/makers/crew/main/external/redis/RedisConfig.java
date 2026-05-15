package org.sopt.makers.crew.main.external.redis;

import org.springframework.context.annotation.Configuration;

@Configuration
//@EnableCaching
//@Profile("lambda-dev")
public class RedisConfig {
	//
	// @Value("${spring.data.redis.host}")
	// private String host;
	//
	// @Value("${spring.data.redis.port}")
	// private int port;
	//
	// @Bean
	// public RedisConnectionFactory redisConnectionFactory() {
	// 	RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
	// 	config.setHostName(host);
	// 	config.setPort(port);
	// 	return new LettuceConnectionFactory(config);
	// }
	//
	// @Bean
	// public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
	// 	RedisTemplate<String, Object> template = new RedisTemplate<>();
	// 	template.setConnectionFactory(redisConnectionFactory);
	//
	// 	ObjectMapper objectMapper = createObjectMapper();
	// 	StringRedisSerializer stringSerializer = new StringRedisSerializer();
	// 	GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
	//
	// 	template.setKeySerializer(stringSerializer);
	// 	template.setHashKeySerializer(stringSerializer);
	// 	template.setValueSerializer(jsonSerializer);
	// 	template.setHashValueSerializer(jsonSerializer);
	// 	template.afterPropertiesSet();
	// 	return template;
	// }
	//
	// @Bean
	// public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
	// 	ObjectMapper objectMapper = createObjectMapper();
	// 	GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
	//
	// 	RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
	// 		.entryTtl(Duration.ofHours(24))
	// 		.serializeKeysWith(
	// 			RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
	// 		.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));
	//
	// 	return RedisCacheManager.builder(redisConnectionFactory)
	// 		.cacheDefaults(defaultCacheConfig)
	// 		.build();
	// }
	//
	// private ObjectMapper createObjectMapper() {
	// 	ObjectMapper objectMapper = new ObjectMapper();
	// 	objectMapper.registerModule(new JavaTimeModule());
	// 	objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	// 	objectMapper.activateDefaultTyping(
	// 		objectMapper.getPolymorphicTypeValidator(),
	// 		ObjectMapper.DefaultTyping.NON_FINAL);
	// 	return objectMapper;
	// }
}
