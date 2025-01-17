package org.sopt.makers.crew.main.entity.lightning;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.Type;
import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;
import org.sopt.makers.crew.main.entity.lightning.converter.LightningPlaceTypeConverter;
import org.sopt.makers.crew.main.entity.lightning.converter.LightningTimingTypeConverter;
import org.sopt.makers.crew.main.entity.lightning.enums.LightningPlaceType;
import org.sopt.makers.crew.main.entity.lightning.enums.LightningTimingType;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
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

	@Column(name = "lightningTimingType")
	@NotNull
	@Convert(converter = LightningTimingTypeConverter.class)
	private LightningTimingType lightningTimingType;

	@Column(name = "activityStartDate")
	@NotNull
	private LocalDateTime activityStartDate;

	@Column(name = "activityEndDate")
	@NotNull
	private LocalDateTime activityEndDate;

	@Column(name = "lightningPlaceType")
	@NotNull
	@Convert(converter = LightningPlaceTypeConverter.class)
	private LightningPlaceType lightningPlaceType;

	@Column(name = "lightningPlace")
	@NotNull
	private String lightningPlace;

	@Column(name = "minimumCapacity")
	@NotNull
	private int minimumCapacity;

	@Column(name = "maximumCapacity")
	@NotNull
	private int maximumCapacity;

	@Column(name = "createdGeneration")
	@NotNull
	private int createdGeneration;

	@Column(name = "imageURL", columnDefinition = "jsonb")
	@Type(JsonBinaryType.class)
	private List<ImageUrlVO> imageURL;

	@Builder
	public Lightning(Integer leaderUserId, String title, String desc, LightningTimingType lightningTimingType,
		LocalDateTime activityStartDate, LocalDateTime activityEndDate, LightningPlaceType lightningPlaceType,
		String lightningPlace, int minimumCapacity, int maximumCapacity, Integer createdGeneration,
		List<ImageUrlVO> imageURL) {
		this.leaderUserId = leaderUserId;
		this.title = title;
		this.desc = desc;
		this.lightningTimingType = lightningTimingType;
		this.activityStartDate = activityStartDate;
		this.activityEndDate = activityEndDate;
		this.lightningPlaceType = lightningPlaceType;
		this.lightningPlace = lightningPlace;
		this.minimumCapacity = minimumCapacity;
		this.maximumCapacity = maximumCapacity;
		this.createdGeneration = createdGeneration;
		this.imageURL = imageURL;
	}
}
