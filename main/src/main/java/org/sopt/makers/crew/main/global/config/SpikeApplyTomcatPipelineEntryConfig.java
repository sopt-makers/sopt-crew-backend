package org.sopt.makers.crew.main.global.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sopt.makers.crew.main.global.tomcat.SpikeApplyTomcatPipelineEntryValve;

@Configuration
public class SpikeApplyTomcatPipelineEntryConfig {

	@Bean
	WebServerFactoryCustomizer<TomcatServletWebServerFactory> spikeApplyTomcatPipelineEntryCustomizer(
		SpikeApplyTomcatPipelineEntryValve spikeApplyTomcatPipelineEntryValve
	) {
		return factory -> factory.addContextValves(spikeApplyTomcatPipelineEntryValve);
	}
}
