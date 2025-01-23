package org.sopt.makers.crew.main.entity.lightning;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.Type;
import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;
import org.sopt.makers.crew.main.entity.lightning.converter.LightningPlaceTypeConverter;
import org.sopt.makers.crew.main.entity.lightning.converter.LightningTimingTypeConverter;
import org.sopt.makers.crew.main.entity.lightning.enums.LightningPlaceType;
import org.sopt.makers.crew.main.entity.lightning.enums.LightningTimingType;
import org.sopt.makers.crew.main.entity.meeting.enums.EnMeetingStatus;
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
	@Column(name = "meetingId")
	private Integer meetingId;

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

	@Column(name = "startDate")
	@NotNull
	private LocalDateTime startDate;

	@Column(name = "endDate")
	@NotNull
	private LocalDateTime endDate;

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
	public Lightning(Integer leaderUserId, Integer meetingId, String title, String desc,
		LightningTimingType lightningTimingType,
		LocalDateTime startDate, LocalDateTime endDate,
		LocalDateTime activityStartDate, LocalDateTime activityEndDate, LightningPlaceType lightningPlaceType,
		String lightningPlace, int minimumCapacity, int maximumCapacity, Integer createdGeneration,
		List<ImageUrlVO> imageURL) {
		this.leaderUserId = leaderUserId;
		this.meetingId = meetingId;
		this.title = title;
		this.desc = desc;
		this.lightningTimingType = lightningTimingType;
		this.startDate = startDate;
		this.endDate = endDate;
		this.activityStartDate = activityStartDate;
		this.activityEndDate = activityEndDate;
		this.lightningPlaceType = lightningPlaceType;
		this.lightningPlace = lightningPlace;
		this.minimumCapacity = minimumCapacity;
		this.maximumCapacity = maximumCapacity;
		this.createdGeneration = createdGeneration;
		this.imageURL = imageURL;
	}

	public boolean checkLightningMeetingLeader(Integer userId) {
		return this.leaderUserId.equals(userId);
	}

	public int getLightningMeetingStatusValue(LocalDateTime now) {
		return getLightningMeetingStatus(now).getValue();
	}

	public EnMeetingStatus getLightningMeetingStatus(LocalDateTime now) {
		if (now.isBefore(startDate)) {
			return EnMeetingStatus.BEFORE_START;
		}
		if (now.isBefore(endDate)) {
			return EnMeetingStatus.APPLY_ABLE;
		}
		return EnMeetingStatus.RECRUITMENT_COMPLETE;
	}

}
