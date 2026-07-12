package org.sopt.makers.crew.main.admin.v2.dto.mumutext;

import java.time.LocalDateTime;

public enum AdminMumuTextStatus {
	ACTIVE("노출 중", "active"),
	SCHEDULED("예약됨", "scheduled"),
	ENDED("종료됨", "ended");

	private final String displayName;
	private final String cssClass;

	AdminMumuTextStatus(String displayName, String cssClass) {
		this.displayName = displayName;
		this.cssClass = cssClass;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getCssClass() {
		return cssClass;
	}

	public static AdminMumuTextStatus from(LocalDateTime now, LocalDateTime showStartDate,
		LocalDateTime showEndDate) {
		if (!now.isBefore(showStartDate) && now.isBefore(showEndDate)) {
			return ACTIVE;
		}
		if (now.isBefore(showStartDate)) {
			return SCHEDULED;
		}
		return ENDED;
	}
}
