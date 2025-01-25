package org.sopt.makers.crew.main.flash.v2.dto.mapper;

import static org.sopt.makers.crew.main.global.constant.CrewConst.*;

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
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2CreateMeetingForFlashResponseDto;

@Mapper(componentModel = "spring")
public interface FlashMapper {
	@Mapping(source = "meetingV2CreateMeetingForFlashResponseDto.files", target = "imageURL", qualifiedByName = "getImageURL")
	@Mapping(target = "startDate", expression = "java(time.now())")
	@Mapping(source = "meetingV2CreateMeetingForFlashResponseDto.activityStartDate", target = "endDate", qualifiedByName = "getActivityStartDate")
	@Mapping(source = "meetingV2CreateMeetingForFlashResponseDto.activityStartDate", target = "activityStartDate", qualifiedByName = "getActivityStartDate")
	@Mapping(source = "meetingV2CreateMeetingForFlashResponseDto.activityEndDate", target = "activityEndDate", qualifiedByName = "getActivityEndDate")
	@Mapping(source = "meetingV2CreateMeetingForFlashResponseDto.flashPlaceType", target = "flashPlaceType", qualifiedByName = "getFlashPlaceType")
	@Mapping(source = "meetingV2CreateMeetingForFlashResponseDto.flashTimingType", target = "flashTimingType", qualifiedByName = "getFlashTimingType")
	Flash toFlashntity(
		MeetingV2CreateMeetingForFlashResponseDto meetingV2CreateMeetingForFlashResponseDto,
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
}
