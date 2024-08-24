package org.sopt.makers.crew.main.external.s3.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = {AwsProperties.class})
public class AwsPropertiesConfiguration {
}
