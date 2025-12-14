package org.sopt.makers.crew.main.entity.soptmap;

import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;
import org.sopt.makers.crew.main.soptmap.service.dto.CreateSoptMapDto;
import org.sopt.makers.crew.main.soptmap.service.dto.UpdateSoptMapDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "sopt_map")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SoptMap extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb", name = "nearby_station_ids")
	private List<Long> nearbyStationIds; // 주변 역 Id

	@Column(name = "place_name")
	private String placeName; // 장소명

	@Column(name = "description")
	private String description; // 한줄 소개

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "map_tags", columnDefinition = "jsonb")
	private List<MapTag> mapTags; // 지도 태그

	@Column(name = "naver_link")
	private String naverLink; // naverLink

	@Column(name = "kakao_link")
	private String kakaoLink; // kakaoLink

	@Column(name = "creator_id")
	private Long creatorId; // 작성자id

	@Builder
	private SoptMap(String placeName, Long creatorId, String description, List<MapTag> mapTags, String naverLink,
		String kakaoLink, List<Long> nearbyStationIds) {
		this.placeName = placeName;
		this.creatorId = creatorId;
		this.description = description;
		this.mapTags = mapTags;
		this.naverLink = naverLink;
		this.kakaoLink = kakaoLink;
		this.nearbyStationIds = nearbyStationIds;
	}

	public static SoptMap create(Integer creatorId, CreateSoptMapDto dto, List<Long> nearbyStationIds) {
		return SoptMap.builder()
			.placeName(dto.getPlaceName())
			.creatorId(Long.valueOf(creatorId))
			.description(dto.getDescription())
			.mapTags(dto.getTags())
			.naverLink(dto.getNaverLink())
			.kakaoLink(dto.getKakaoLink())
			.nearbyStationIds(nearbyStationIds)
			.build();
	}

	public void update(UpdateSoptMapDto dto, List<Long> nearbyStationIds) {
		this.placeName = dto.getPlaceName();
		this.description = dto.getDescription();
		this.mapTags = dto.getTags();
		this.naverLink = dto.getNaverLink();
		this.kakaoLink = dto.getKakaoLink();
		this.nearbyStationIds = nearbyStationIds;
	}
}
