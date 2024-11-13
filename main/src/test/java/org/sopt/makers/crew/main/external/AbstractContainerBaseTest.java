package org.sopt.makers.crew.main.external;

import java.util.function.Supplier;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public abstract class AbstractContainerBaseTest {
	static final String REDIS_IMAGE = "redis:6-alpine";
	static final GenericContainer REDIS_CONTAINER;

	static {
		REDIS_CONTAINER = new GenericContainer<>(REDIS_IMAGE)
			.withExposedPorts(6379)
			.withReuse(true)
			.waitingFor(Wait.forListeningPort());
		REDIS_CONTAINER.start();
	}

	@DynamicPropertySource
	public static void overrideProps(DynamicPropertyRegistry registry) {
		Supplier<Object> getHost = REDIS_CONTAINER::getHost;
		registry.add("spring.redis.host", getHost);
		registry.add("spring.redis.port", () -> "" + REDIS_CONTAINER.getMappedPort(6379));
	}
}