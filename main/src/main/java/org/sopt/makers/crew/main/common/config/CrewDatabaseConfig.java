package org.sopt.makers.crew.main.common.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
	basePackages = "org.sopt.makers.crew.main.entity", // 첫번째 DB가 있는 패키지(폴더) 경로
	entityManagerFactoryRef = "primaryEntityManagerFactory", // EntityManager의 이름
	transactionManagerRef = "primaryTransactionManager" // 트랜잭션 매니저의 이름
)
@Profile({"dev", "prod"})
public class CrewDatabaseConfig {

	@Bean
	@Primary
	@ConfigurationProperties("spring.datasource")
	public DataSource primaryDatasourceProperties() {
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "primaryEntityManagerFactory")
	@Primary
	public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory() {
		DataSource dataSource = primaryDatasourceProperties();

		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(dataSource);
		em.setPackagesToScan("org.sopt.makers.crew.main.entity");

		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setShowSql(true);
		em.setJpaVendorAdapter(vendorAdapter);

		Map<String, Object> properties = new HashMap<>();
		properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
		properties.put("hibernate.hbm2ddl.auto", "validate");
		properties.put("hibernate.format_sql", true);
		properties.put("hibernate.physical_naming_strategy", "org.sopt.makers.crew.main.common.config.CamelCaseNamingStrategy");
		properties.put("hibernate.globally_quoted_identifiers", true);
		em.setJpaPropertyMap(properties);

		return em;
	}

	@Bean(name = "primaryTransactionManager")
	@Primary
	public PlatformTransactionManager primaryTransactionManager(
		final @Qualifier("primaryEntityManagerFactory") LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean
	) {
		return new JpaTransactionManager(Objects.requireNonNull(localContainerEntityManagerFactoryBean.getObject()));
	}
}
