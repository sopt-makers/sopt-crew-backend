package org.sopt.makers.crew.main.global.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
	basePackages = "org.sopt.makers.crew.main.entity", // 첫번째 DB가 있는 패키지(폴더) 경로
	entityManagerFactoryRef = "primaryEntityManagerFactory", // EntityManager의 이름
	transactionManagerRef = "primaryTransactionManager" // 트랜잭션 매니저의 이름
)
@Profile({"local", "dev", "prod", "test", "traffic"})
public class CrewDatabaseConfig {

	@Bean
	@Primary
	@ConfigurationProperties("spring.datasource.hikari")
	public DataSource primaryDatasourceProperties() {
		return DataSourceBuilder.create()
				.type(HikariDataSource.class)
				.build();
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
		properties.put("hibernate.format_sql", true);
		properties.put("hibernate.physical_naming_strategy",
			"org.sopt.makers.crew.main.global.config.CamelCaseNamingStrategy");

		/**
		 * local, dev, prod 환경은 validate 로 한다.
		 * test 환경일 경우, none(기본값) 으로 한다. schema.sql 을 사용하여 테이블을 생성한다. (하단 코드 참고)
		 */

		String[] activeProfiles = {"local", "dev", "prod"};
		if (Arrays.stream(activeProfiles)
			.anyMatch(profile -> profile.equals(System.getProperty("spring.profiles.active")))) {
			properties.put("hibernate.hbm2ddl.auto", "validate");
		}
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

	private final ResourceLoader resourceLoader;

	public CrewDatabaseConfig(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Bean
	@Profile("test")
	public CommandLineRunner init(@Qualifier("primaryEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
		return args -> executeSchemaSql();
	}

	private void executeSchemaSql() throws Exception {
		Resource resource = resourceLoader.getResource("classpath:schema.sql");
		ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
		databasePopulator.addScript(resource);
		try {
			databasePopulator.populate(primaryDatasourceProperties().getConnection());
		} catch (Exception e) {
			throw e;
		}
	}
}
