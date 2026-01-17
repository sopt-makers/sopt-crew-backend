package org.sopt.makers.crew.main.global.util;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtils {

	private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4})[-/.](\\d{1,2})[-/.](\\d{1,2})");

	/**
	 * 다양한 구분자로 표현된 날짜 문자열을 LocalDate로 변환합니다.
	 * 지원 형식: yyyy-MM-dd, yyyy/MM/dd, yyyy.MM.dd
	 *
	 * @param date 날짜 문자열 (예: "2025-02-03", "2025/02/03", "2025.02.03")
	 * @return LocalDate 객체
	 * @throws IllegalArgumentException 날짜 형식이 올바르지 않은 경우
	 */
	public static LocalDate toLocalDate(String date) {
		if (date == null || date.isBlank()) {
			throw new IllegalArgumentException("날짜는 null이거나 빈 값일 수 없습니다.");
		}

		Matcher matcher = DATE_PATTERN.matcher(date);
		if (!matcher.matches()) {
			throw new IllegalArgumentException(
				"날짜 형식이 올바르지 않습니다. 지원 형식: yyyy-MM-dd, yyyy/MM/dd, yyyy.MM.dd");
		}

		int year = Integer.parseInt(matcher.group(1));
		int month = Integer.parseInt(matcher.group(2));
		int day = Integer.parseInt(matcher.group(3));

		return LocalDate.of(year, month, day);
	}
}
