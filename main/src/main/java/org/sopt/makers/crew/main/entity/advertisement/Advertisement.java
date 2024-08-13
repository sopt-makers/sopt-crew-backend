package org.sopt.makers.crew.main.entity.advertisement;

import static jakarta.persistence.GenerationType.*;

import java.time.LocalDateTime;

import org.sopt.makers.crew.main.entity.advertisement.enums.AdvertisementCategory;
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
@EntityListeners(AuditingEntityListener.class)
@Table(name = "advertisement")
public class Advertisement {
	/**
	 * Primary Key
	 */
	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Integer id;

	@NotNull
	private String advertisementImageUrl;

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

	@Builder
	private Advertisement(String advertisementImageUrl, String advertisementLink,
		AdvertisementCategory advertisementCategory, Long priority, LocalDateTime advertisementStartDate,
		LocalDateTime advertisementEndDate) {
		this.advertisementImageUrl = advertisementImageUrl;
		this.advertisementLink = advertisementLink;
		this.advertisementCategory = advertisementCategory;
		this.priority = priority;
		this.advertisementStartDate = advertisementStartDate;
		this.advertisementEndDate = advertisementEndDate;
	}
}
