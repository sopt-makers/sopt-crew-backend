package org.sopt.makers.crew.main.external.caffeine;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableCaching
@Profile("!lambda-dev")
public class CaffeineConfig {

}
