package org.sopt.makers.crew.main.entity.soptmap;

import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "map_recommended")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
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
}
