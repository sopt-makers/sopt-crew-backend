package org.sopt.makers.crew.main.entity.property;

import java.util.Map;

import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "property")
public class Property {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, name = "key", unique = true)
	private String key;

	@Type(JsonBinaryType.class)
	@Column(name = "properties")
	private Map<String, Object> properties;

	@Builder
	public Property(String key, Map<String, Object> properties) {
		this.key = key;
		this.properties = properties;
	}

	public void updateProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
}
