package org.sopt.makers.crew.main.common.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class CamelCaseNamingStrategy implements PhysicalNamingStrategy {

	@Override
	public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		return apply(name, jdbcEnvironment);
	}

	@Override
	public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		return apply(name, jdbcEnvironment);
	}

	@Override
	public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		return apply(name, jdbcEnvironment);
	}

	@Override
	public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		return apply(name, jdbcEnvironment);
	}

	@Override
	public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		return apply(name, jdbcEnvironment);
	}

	private Identifier apply(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
		if (identifier == null) {
			return null;
		}
		return Identifier.toIdentifier(identifier.getText(), true);
	}
}
