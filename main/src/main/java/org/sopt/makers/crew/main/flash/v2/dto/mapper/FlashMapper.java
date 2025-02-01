package org.sopt.makers.crew.main.flash.v2.dto.mapper;

import static org.sopt.makers.crew.main.global.constant.CrewConst.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.sopt.makers.crew.main.entity.flash.Flash;
import org.sopt.makers.crew.main.entity.flash.enums.FlashPlaceType;
import org.sopt.makers.crew.main.entity.flash.enums.FlashTimingType;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2CreateAndUpdateMeetingForFlashResponseDto;

@Mapper(componentModel = "spring")
public interface FlashMapper {
	@Mapping(source = "meetingV2CreateAndUpdateMeetingForFlashResponseDto.files", target = "imageURL", qualifiedByName = "getImageURL")
	@Mapping(target = "startDate", expression = "java(time.now())")
	@Mapping(source = "meetingV2CreateAndUpdateMeetingForFlashResponseDto.activityStartDate", target = "endDate", qualifiedByName = "getPreviousDayEndTime")
	@Mapping(source = "meetingV2CreateAndUpdateMeetingForFlashResponseDto.activityStartDate", target = "activityStartDate", qualifiedByName = "getActivityStartDate")
	@Mapping(source = "meetingV2CreateAndUpdateMeetingForFlashResponseDto.activityEndDate", target = "activityEndDate", qualifiedByName = "getActivityEndDate")
	@Mapping(source = "meetingV2CreateAndUpdateMeetingForFlashResponseDto.flashPlaceType", target = "flashPlaceType", qualifiedByName = "getFlashPlaceType")
	@Mapping(source = "meetingV2CreateAndUpdateMeetingForFlashResponseDto.flashTimingType", target = "flashTimingType", qualifiedByName = "getFlashTimingType")
	Flash toFlashEntity(
		MeetingV2CreateAndUpdateMeetingForFlashResponseDto meetingV2CreateAndUpdateMeetingForFlashResponseDto,
		Integer createdGeneration, Integer leaderUserId, Time time);

	@Named("getImageURL")
	static List<ImageUrlVO> getImageURL(List<String> files) {
		return IntStream.range(0, files.size())
			.mapToObj(index -> new ImageUrlVO(index, files.get(index)))
			.toList();
	}

	@Named("getActivityStartDate")
	static LocalDateTime getActivityStartDate(String date) {
		return LocalDateTime.parse(date + DAY_START_TIME, DateTimeFormatter.ofPattern(DAY_TIME_FORMAT));
	}

	@Named("getActivityEndDate")
	static LocalDateTime getActivityEndDate(String date) {
		return LocalDateTime.parse(date + DAY_END_TIME, DateTimeFormatter.ofPattern(DAY_TIME_FORMAT));
	}

	@Named("getFlashPlaceType")
	static FlashPlaceType getFlashPlaceType(String placeType) {
		return FlashPlaceType.ofValue(placeType);
	}

	@Named("getFlashTimingType")
	static FlashTimingType getFlashTimingType(String timingType) {
		return FlashTimingType.ofValue(timingType);
	}

	@Named("getPreviousDayEndTime")
	static LocalDateTime getPreviousDayEndTime(String date) {
		return LocalDate.parse(date, DateTimeFormatter.ofPattern(DAY_FORMAT))
			.minusDays(1)
			.atTime(DAY_END_HOUR, DAY_END_MINUTE, DAY_END_SECOND);
	}
}
