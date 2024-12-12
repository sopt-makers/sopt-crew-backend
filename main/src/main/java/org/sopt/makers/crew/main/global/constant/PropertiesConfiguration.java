package org.sopt.makers.crew.main.global.constant;

import org.sopt.makers.crew.main.external.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class PropertiesConfiguration {

}
