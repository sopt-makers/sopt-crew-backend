package org.sopt.makers.crew.main.entity.soptmap;

import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;

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
@Table(name = "map_recommended")
public class MapRecommend extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "sopt_map_id")
	private Long soptMapId;

	@Column(name = "active")
	private Boolean active;

	@Builder
	private MapRecommend(Long userId, Long soptMapId, Boolean active) {
		this.userId = userId;
		this.soptMapId = soptMapId;
		this.active = active;
	}

	public static MapRecommend create(Long userId, Long soptMapId) {
		return MapRecommend.builder()
			.userId(userId)
			.soptMapId(soptMapId)
			.active(true)
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof MapRecommend mapRecommend))
			return false;
		return this.getId() != null && this.getId().equals(mapRecommend.getId());
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(this.getId());
	}

	public void deleteRecommend() {
		this.active = false;
	}

	public void toggleStatus() {
		this.active = !this.active;
	}
}
