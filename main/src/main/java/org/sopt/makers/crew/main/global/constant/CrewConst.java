package org.sopt.makers.crew.main.global.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CrewConst {

	/**
	 * 매 기수 시작하기 전에 수정 필요
	 * */
	public static final Integer ACTIVE_GENERATION = 37;

	public static final String DAY_START_TIME = " 00:00:00";
	public static final String DAY_END_TIME = " 23:59:59";

	public static final String DAY_FORMAT = "yyyy.MM.dd";
	public static final String DAY_TIME_FORMAT = "yyyy.MM.dd HH:mm:ss";

	public static final String ORDER_ASC = "asc";
	public static final String ORDER_DESC = "desc";

	public static final int DAY_END_HOUR = 23;
	public static final int DAY_END_MINUTE = 59;
	public static final int DAY_END_SECOND = 59;
}
