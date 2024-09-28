package org.sopt.makers.crew.main.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@EnableJpaAuditing
@Configuration
public class JpaAuditingConfig {

	@PersistenceContext(unitName = "primaryEntityManagerFactory")
	private EntityManager primaryEntityManager;

	@PersistenceContext(unitName = "secondEntityManagerFactory")
	private EntityManager playgroundEntityManager;

	@Bean(name = "primaryQueryFactory")
	@Primary
	public JPAQueryFactory primaryQueryFactory() {
		return new JPAQueryFactory(JPQLTemplates.DEFAULT, primaryEntityManager);
	}

	@Bean(name = "playgroundQueryFactory")
	public JPAQueryFactory playgroundQueryFactory() {
		return new JPAQueryFactory(JPQLTemplates.DEFAULT, playgroundEntityManager);
	}
}
