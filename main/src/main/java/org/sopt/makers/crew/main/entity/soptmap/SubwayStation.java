package org.sopt.makers.crew.main.entity.soptmap;

import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;
import org.sopt.makers.crew.main.soptmap.service.dto.SubwayStationDto;

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

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "subway_station")
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

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof SubwayStation subwayStation))
			return false;
		return this.getId() != null && this.getId().equals(subwayStation.getId());
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(this.getId());
	}

	public SubwayStationDto toDto() {
		return new SubwayStationDto(id, name, SubwayLine.fromValues(lines));
	}
}
