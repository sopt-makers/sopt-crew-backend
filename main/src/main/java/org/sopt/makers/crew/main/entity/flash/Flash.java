package org.sopt.makers.crew.main.entity.flash;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.Type;
import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;
import org.sopt.makers.crew.main.entity.flash.converter.FlashPlaceTypeConverter;
import org.sopt.makers.crew.main.entity.flash.converter.FlashTimingTypeConverter;
import org.sopt.makers.crew.main.entity.flash.enums.FlashPlaceType;
import org.sopt.makers.crew.main.entity.flash.enums.FlashTimingType;
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
@Table(name = "flash")
public class Flash extends BaseTimeEntity {
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

	@Column(name = "flashTimingType")
	@NotNull
	@Convert(converter = FlashTimingTypeConverter.class)
	private FlashTimingType flashTimingType;

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

	@Column(name = "flashPlaceType")
	@NotNull
	@Convert(converter = FlashPlaceTypeConverter.class)
	private FlashPlaceType flashPlaceType;

	@Column(name = "flashPlace")
	private String flashPlace;

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
	public Flash(Integer leaderUserId, Integer meetingId, String title, String desc,
		FlashTimingType flashTimingType,
		LocalDateTime startDate, LocalDateTime endDate,
		LocalDateTime activityStartDate, LocalDateTime activityEndDate, FlashPlaceType flashPlaceType,
		String flashPlace, int minimumCapacity, int maximumCapacity, Integer createdGeneration,
		List<ImageUrlVO> imageURL) {
		this.leaderUserId = leaderUserId;
		this.meetingId = meetingId;
		this.title = title;
		this.desc = desc;
		this.flashTimingType = flashTimingType;
		this.startDate = startDate;
		this.endDate = endDate;
		this.activityStartDate = activityStartDate;
		this.activityEndDate = activityEndDate;
		this.flashPlaceType = flashPlaceType;
		this.flashPlace = flashPlace;
		this.minimumCapacity = minimumCapacity;
		this.maximumCapacity = maximumCapacity;
		this.createdGeneration = createdGeneration;
		this.imageURL = imageURL;
	}

	public void updateFlash(Flash updatedFlash) {
		this.leaderUserId = updatedFlash.leaderUserId;
		this.meetingId = updatedFlash.meetingId;
		this.title = updatedFlash.title;
		this.desc = updatedFlash.desc;
		this.flashTimingType = updatedFlash.flashTimingType;
		this.startDate = updatedFlash.startDate;
		this.endDate = updatedFlash.endDate;
		this.activityStartDate = updatedFlash.activityStartDate;
		this.activityEndDate = updatedFlash.activityEndDate;
		this.flashPlaceType = updatedFlash.flashPlaceType;
		this.flashPlace = updatedFlash.flashPlace;
		this.minimumCapacity = updatedFlash.minimumCapacity;
		this.maximumCapacity = updatedFlash.maximumCapacity;
		this.createdGeneration = updatedFlash.createdGeneration;
		this.imageURL = updatedFlash.imageURL;
	}

	public boolean checkFlashMeetingLeader(Integer userId) {
		return this.leaderUserId.equals(userId);
	}

	public int getFlashMeetingStatusValue(LocalDateTime now) {
		return getFlashMeetingStatus(now).getValue();
	}

	public EnMeetingStatus getFlashMeetingStatus(LocalDateTime now) {
		if (now.isBefore(startDate)) {
			return EnMeetingStatus.BEFORE_START;
		}
		if (now.isBefore(endDate)) {
			return EnMeetingStatus.APPLY_ABLE;
		}
		return EnMeetingStatus.RECRUITMENT_COMPLETE;
	}

	public void updateLeaderUserId(Integer newLeaderUserId) {
		this.leaderUserId = newLeaderUserId;
	}
}
