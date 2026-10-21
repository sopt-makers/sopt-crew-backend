package org.sopt.makers.crew.main.dining.v2.dto.mapper;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.IntStream;

import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 회식을 meeting 행으로 변환한다. (번쩍의 FlashMeetingMapper 와 같은 방식)
 * 회식에서 쓰지 않는 meeting 의 NOT NULL 컬럼은 정책 고정값으로 채운다.
 */
@Component
public class DiningMeetingMapper {

	private static final DateTimeFormatter DINING_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
	private static final DateTimeFormatter START_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
	private static final int UNUSED_CAPACITY = 0;
	private static final String UNUSED_TEXT = "";

	public Meeting toMeetingEntity(String title, List<ImageUrlVO> imageURL, LocalDate diningDate,
		MeetingJoinablePart[] joinableParts, Integer userId, LocalDateTime now, Integer activeGeneration) {
		return Meeting.builder()
			.userId(userId)
			.title(title)
			.category(MeetingCategory.DINING)
			.imageURL(imageURL)
			.startDate(now)
			.endDate(toEndOfDay(diningDate))
			.capacity(UNUSED_CAPACITY)
			.desc(UNUSED_TEXT)
			.processDesc(UNUSED_TEXT)
			.mStartDate(toStartOfDay(diningDate))
			.mEndDate(toEndOfDay(diningDate))
			.leaderDesc(UNUSED_TEXT)
			.note(UNUSED_TEXT)
			.isMentorNeeded(false)
			.canJoinOnlyActiveGeneration(false)
			.createdGeneration(activeGeneration)
			.targetActiveGeneration(null)
			.joinableParts(joinableParts)
			.build();
	}

	public static LocalDate parseDiningDate(String diningDate) {
		try {
			return LocalDate.parse(diningDate, DINING_DATE_FORMATTER);
		} catch (DateTimeParseException | NullPointerException e) {
			throw new BadRequestException(VALIDATION_EXCEPTION.getErrorCode());
		}
	}

	public static LocalTime parseStartTime(String startTime) {
		if (!StringUtils.hasText(startTime)) {
			return null;
		}
		try {
			return LocalTime.parse(startTime, START_TIME_FORMATTER);
		} catch (DateTimeParseException e) {
			throw new BadRequestException(VALIDATION_EXCEPTION.getErrorCode());
		}
	}

	public static LocalDateTime toStartOfDay(LocalDate date) {
		return date.atStartOfDay();
	}

	public static LocalDateTime toEndOfDay(LocalDate date) {
		return date.atTime(23, 59, 59);
	}

	public static List<ImageUrlVO> toImageUrls(List<String> files) {
		return IntStream.range(0, files.size())
			.mapToObj(index -> new ImageUrlVO(index, files.get(index)))
			.toList();
	}

	/**
	 * 빈 문자열로 들어온 선택 입력값(장소, 링크)은 null 로 저장한다.
	 */
	public static String blankToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
