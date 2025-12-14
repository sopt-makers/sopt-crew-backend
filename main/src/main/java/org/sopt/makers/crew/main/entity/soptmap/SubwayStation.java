package org.sopt.makers.crew.main.entity.soptmap;

import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "subway_station")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SubwayStation extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name")
	private String name;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb", name = "lines")
	private List<SubwayLine> lines;

	@Builder
	private SubwayStation(String name, List<SubwayLine> lines) {
		this.name = name;
		this.lines = lines;
	}
}
