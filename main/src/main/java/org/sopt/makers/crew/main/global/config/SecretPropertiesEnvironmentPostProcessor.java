package org.sopt.makers.crew.main.global.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * Lambda 환경에서 설정 파일 로딩 문제를 해결하기 위한 EnvironmentPostProcessor.
 * 'lambda-dev' 프로파일 활성화 시, 설정 파일을 명시적으로 로드하여 표준 Spring Boot 로딩 메커니즘을 보완합니다.
 */
public class SecretPropertiesEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String TARGET_PROFILE = "lambda-dev";
    private static final String SECRET_PROPERTIES = "application-secret.properties";
    private static final String YAML_PATTERN = "application-%s.yml";
    private static final String ENV_SPRING_PROFILES_ACTIVE = "SPRING_PROFILES_ACTIVE";

    private final DeferredLog log = new DeferredLog();
    private final YamlPropertySourceLoader yamlLoader = new YamlPropertySourceLoader();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Set<String> activeProfiles = resolveActiveProfiles(environment);

        if (activeProfiles.contains(TARGET_PROFILE)) {
            loadYamlConfigurations(environment, activeProfiles);
            loadSecretConfiguration(environment);
            log.replayTo(SecretPropertiesEnvironmentPostProcessor.class);
        }
    }

    private Set<String> resolveActiveProfiles(ConfigurableEnvironment environment) {
        Set<String> profiles = Arrays.stream(environment.getActiveProfiles())
                .collect(Collectors.toSet());

        // 환경 변수에서 프로파일 확인 후 병합 (기존 프로파일 유무와 관계없이 수행하여 안전성 확보)
        String envProfiles = System.getenv(ENV_SPRING_PROFILES_ACTIVE);
        if (StringUtils.hasText(envProfiles)) {
            Stream.of(envProfiles.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .forEach(profiles::add);
        }

        return profiles;
    }

    private void loadYamlConfigurations(ConfigurableEnvironment environment, Set<String> profiles) {
        profiles.stream()
                .map(profile -> String.format(YAML_PATTERN, profile))
                .forEach(filename -> loadSingleYamlConfiguration(environment, filename));
    }

    private void loadSingleYamlConfiguration(ConfigurableEnvironment environment, String filename) {
        Resource resource = new ClassPathResource(filename);
        if (!resource.exists()) {
            return;
        }

        try {
            var sources = yamlLoader.load(filename, resource);
            sources.forEach(source -> environment.getPropertySources().addLast(source));
            log.info("Loaded configuration: " + filename);
        } catch (IOException e) {
            log.warn("Failed to load configuration: " + filename);
        }
    }

    private void loadSecretConfiguration(ConfigurableEnvironment environment) {
        Resource resource = new ClassPathResource(SECRET_PROPERTIES);

        if (!resource.exists()) {
            log.warn("Secret configuration not found: " + SECRET_PROPERTIES);
            return;
        }

        try {
            Properties properties = new Properties();
            properties.load(resource.getInputStream());
            environment.getPropertySources().addFirst(
                    new PropertiesPropertySource("application-secret", properties));
            log.info("Loaded secret configuration: " + SECRET_PROPERTIES);
        } catch (IOException e) {
            log.error("Failed to load secret configuration", e);
            throw new RuntimeException("Failed to load secret properties", e);
        }
    }
}
