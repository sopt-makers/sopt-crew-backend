package org.sopt.makers.crew.main.common.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients(basePackages = "org.sopt.makers.crew.main.internal.notification")
@Configuration
public class FeignClientConfig {

}
