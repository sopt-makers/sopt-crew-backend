package org.sopt.makers.crew.main.entity.advertisement;

import static jakarta.persistence.GenerationType.*;

import java.time.LocalDateTime;

import org.sopt.makers.crew.main.entity.advertisement.enums.AdvertisementCategory;
import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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

	@Builder
	private Advertisement(String advertisementDesktopImageUrl, String advertisementMobileImageUrl,
		String advertisementLink,
		AdvertisementCategory advertisementCategory, Long priority, LocalDateTime advertisementStartDate,
		LocalDateTime advertisementEndDate, boolean isSponsoredContent) {
		this.advertisementDesktopImageUrl = advertisementDesktopImageUrl;
		this.advertisementMobileImageUrl = advertisementMobileImageUrl;
		this.advertisementLink = advertisementLink;
		this.advertisementCategory = advertisementCategory;
		this.priority = priority;
		this.advertisementStartDate = advertisementStartDate;
		this.advertisementEndDate = advertisementEndDate;
		this.isSponsoredContent = isSponsoredContent;
	}
}