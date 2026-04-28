package org.sopt.makers.crew.main.entity.advertisement;

import static jakarta.persistence.GenerationType.*;

import java.time.LocalDateTime;

import org.sopt.makers.crew.main.entity.advertisement.enums.AdvertisementCategory;
import org.sopt.makers.crew.main.entity.advertisement.enums.EventType;
import org.sopt.makers.crew.main.entity.advertisement.enums.TargetGeneration;
import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "advertisement")
public class Advertisement extends BaseTimeEntity {
	/**
	 * Primary Key
	 */
	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Integer id;

	@NotNull
	private String advertisementDesktopImageUrl;

	@NotNull
	private String advertisementMobileImageUrl;

	@NotNull
	private String advertisementLink;

	private String calendarImageUrl;

	private String titlePrefix;

	private String titleHighlight;

	private String titleSuffix;

	private String subTitle;

	@NotNull
	@Enumerated(EnumType.STRING)
	private AdvertisementCategory advertisementCategory;

	@NotNull
	private Long priority;

	@NotNull
	private LocalDateTime advertisementStartDate;

	@NotNull
	private LocalDateTime advertisementEndDate;

	@NotNull
	private boolean isSponsoredContent;

	@NotNull
	private boolean isDisplay;

	/**
	 * 배너 이벤트 타입
	 */
	@Enumerated(EnumType.STRING)
	private EventType eventType;

	/**
	 * 광고 노출 기수
	 */
	@NotNull
	@Enumerated(EnumType.STRING)
	private TargetGeneration targetGeneration;

	@Builder
	private Advertisement(String advertisementDesktopImageUrl, String advertisementMobileImageUrl,
		String advertisementLink, String calendarImageUrl, String titlePrefix, String titleHighlight,
		String titleSuffix, String subTitle,
		AdvertisementCategory advertisementCategory, Long priority, LocalDateTime advertisementStartDate,
		LocalDateTime advertisementEndDate, boolean isSponsoredContent, Boolean isDisplay, EventType eventType,
		TargetGeneration targetGeneration) {
		this.advertisementDesktopImageUrl = advertisementDesktopImageUrl;
		this.advertisementMobileImageUrl = advertisementMobileImageUrl;
		this.advertisementLink = advertisementLink;
		this.calendarImageUrl = calendarImageUrl;
		this.titlePrefix = titlePrefix;
		this.titleHighlight = titleHighlight;
		this.titleSuffix = titleSuffix;
		this.subTitle = subTitle;
		this.advertisementCategory = advertisementCategory;
		this.priority = priority;
		this.advertisementStartDate = advertisementStartDate;
		this.advertisementEndDate = advertisementEndDate;
		this.isSponsoredContent = isSponsoredContent;
		this.isDisplay = isDisplay == null || isDisplay;
		this.eventType = eventType;
		this.targetGeneration = targetGeneration == null ? TargetGeneration.ALL : targetGeneration;
	}

	public void updateDisplay(boolean isDisplay) {
		this.isDisplay = isDisplay;
	}
}
