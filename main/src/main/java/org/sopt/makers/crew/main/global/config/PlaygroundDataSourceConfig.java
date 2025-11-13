package org.sopt.makers.crew.main.global.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
	basePackages = "org.sopt.makers.crew.main.external.playground.entity",
	entityManagerFactoryRef = "secondEntityManagerFactory",
	transactionManagerRef = "secondTransactionManager"
)
@Profile({"local", "dev", "prod", "test", "traffic"})
public class PlaygroundDataSourceConfig {
	@Bean
	@ConfigurationProperties("spring.playground-datasource.hikari")
	public HikariDataSource secondDatasource() {
		return new HikariDataSource();
	}

	@Bean(name = "secondEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean secondEntityManagerFactory() {
		DataSource dataSource = secondDatasource();

		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(dataSource);
		em.setPackagesToScan("org.sopt.makers.crew.main.external.playground.entity");

		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setShowSql(true);
		em.setJpaVendorAdapter(vendorAdapter);

		Map<String, Object> properties = new HashMap<>();
		properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
		properties.put("hibernate.format_sql", true);
		properties.put("hibernate.physical_naming_strategy",
			"org.sopt.makers.crew.main.global.config.CamelCaseNamingStrategy");

		/**
		 * local, dev, prod 환경은 validate 로 한다.
		 * test 환경일 경우, none(기본값) 으로 한다. schema.sql 을 사용하여 테이블을 생성한다.
		 */

		String[] activeProfiles = {"local", "dev", "prod"};
		if (Arrays.stream(activeProfiles)
			.anyMatch(profile -> profile.equals(System.getProperty("spring.profiles.active")))) {
			properties.put("hibernate.hbm2ddl.auto", "validate");
		}

		em.setJpaPropertyMap(properties);

		return em;
	}

	@Bean(name = "secondTransactionManager")
	public PlatformTransactionManager secondTransactionManager(
		final @Qualifier("secondEntityManagerFactory") LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean
	) {
		return new JpaTransactionManager(Objects.requireNonNull(localContainerEntityManagerFactoryBean.getObject()));
	}
}
