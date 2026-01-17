package org.sopt.makers.crew.main.entity.property;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PropertyKeys {

	HOME("home"),
	OFFICIAL_DATE("officialDateText"),
	EVENT_NUM("eventNumbers"),
	START_DATE("startDate"),
	END_DATE("endDate"),
	SOPT_MAP_EVENT("soptMapEvent");

	private final String key;

}
