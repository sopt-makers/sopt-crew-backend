package org.sopt.makers.crew.main.lightning.v2.dto.mapper;

import static org.sopt.makers.crew.main.global.constant.CrewConst.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.sopt.makers.crew.main.entity.lightning.Lightning;
import org.sopt.makers.crew.main.entity.lightning.enums.LightningPlaceType;
import org.sopt.makers.crew.main.entity.lightning.enums.LightningTimingType;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2CreateMeetingForLightningResponseDto;

@Mapper(componentModel = "spring")
public interface LightningMapper {
	@Mapping(source = "meetingV2CreateMeetingForLightningResponseDto.files", target = "imageURL", qualifiedByName = "getImageURL")
	@Mapping(target = "startDate", expression = "java(time.now())")
	@Mapping(source = "meetingV2CreateMeetingForLightningResponseDto.activityStartDate", target = "endDate", qualifiedByName = "getActivityStartDate")
	@Mapping(source = "meetingV2CreateMeetingForLightningResponseDto.activityStartDate", target = "activityStartDate", qualifiedByName = "getActivityStartDate")
	@Mapping(source = "meetingV2CreateMeetingForLightningResponseDto.activityEndDate", target = "activityEndDate", qualifiedByName = "getActivityEndDate")
	@Mapping(source = "meetingV2CreateMeetingForLightningResponseDto.lightningPlaceType", target = "lightningPlaceType", qualifiedByName = "getLightningPlaceType")
	@Mapping(source = "meetingV2CreateMeetingForLightningResponseDto.lightningTimingType", target = "lightningTimingType", qualifiedByName = "getLightningTimingType")
	Lightning toLightningEntity(
		MeetingV2CreateMeetingForLightningResponseDto meetingV2CreateMeetingForLightningResponseDto,
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

	@Named("getLightningPlaceType")
	static LightningPlaceType getLightningPlaceType(String placeType) {
		return LightningPlaceType.ofValue(placeType);
	}

	@Named("getLightningTimingType")
	static LightningTimingType getLightningTimingType(String timingType) {
		return LightningTimingType.ofValue(timingType);
	}
}
