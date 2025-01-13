package org.sopt.makers.crew.main.entity.lightning;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.Type;
import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "lightning")
public class Lightning extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@NotNull
	@Column(name = "leaderUserId")
	private Integer leaderUserId;

	@NotNull
	@Size(min = 1, max = 30)
	@Column(name = "title")
	private String title;

	@Column(name = "desc", columnDefinition = "TEXT")
	private String desc;

	@Column(name = "activityStartDate")
	@NotNull
	private LocalDate activityStartDate;

	@Column(name = "activityEndDate")
	@NotNull
	private LocalDateTime activityEndDate;

	@Column(name = "lightningTime")
	private LocalDateTime lightningTime;

	@Column(name = "lightningPlaceType")
	@NotNull
	@Enumerated(EnumType.STRING)
	private LightningPlaceType lightningPlaceType;

	@Column(name = "lightningPlace")
	@NotNull
	private String lightningPlace;

	@Column(name = "minimumCapacity")
	private int minimumCapacity;

	@Column(name = "maximumCapacity")
	private int maximumCapacity;

	@Column(name = "imageURL", columnDefinition = "jsonb")
	@Type(JsonBinaryType.class)
	private List<ImageUrlVO> imageURL;

}
