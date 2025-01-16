package org.sopt.makers.crew.main.lightning.v2.dto.mapper;

import static org.sopt.makers.crew.main.global.constant.CrewConst.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.sopt.makers.crew.main.entity.lightning.Lightning;
import org.sopt.makers.crew.main.entity.lightning.enums.LightningPlaceType;
import org.sopt.makers.crew.main.entity.lightning.enums.LightningTimingType;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.lightning.v2.dto.request.LightningV2CreateLightningBodyWithoutWelcomeMessageDto;

@Mapper(componentModel = "spring")
public interface LightningMapper {

	@Mapping(source = "lightningBody.files", target = "imageURL", qualifiedByName = "getImageURL")
	@Mapping(source = "lightningBody.activityStartDate", target = "activityStartDate", qualifiedByName = "getStartDate")
	@Mapping(source = "lightningBody.activityEndDate", target = "activityEndDate", qualifiedByName = "getEndDate")
	@Mapping(source = "lightningBody.lightningPlaceType", target = "lightningPlaceType", qualifiedByName = "getLightningPlaceType")
	@Mapping(source = "lightningBody.lightningTimingType", target = "lightningTimingType", qualifiedByName = "getLightningTimingType")
	Lightning toLightningEntity(LightningV2CreateLightningBodyWithoutWelcomeMessageDto lightningBody,
		Integer createdGeneration, Integer leaderUserId);

	@Named("getImageURL")
	static List<ImageUrlVO> getImageURL(List<String> files) {
		AtomicInteger index = new AtomicInteger(0);

		return files.stream()
			.map(fileUrl -> new ImageUrlVO(index.getAndIncrement(), fileUrl))
			.toList();
	}

	@Named("getStartDate")
	static LocalDateTime getStartDate(String date) {
		return LocalDateTime.parse(date + DAY_START_TIME, DateTimeFormatter.ofPattern(DAY_TIME_FORMAT));
	}

	@Named("getEndDate")
	static LocalDateTime getEndDate(String date) {
		return LocalDateTime.parse(date + DAY_END_TIME, DateTimeFormatter.ofPattern(DAY_TIME_FORMAT));

	}

	@Named("getLightningPlaceType")
	static LightningPlaceType getLightningPlaceType(String placeType) {
		return LightningPlaceType.valueOf(placeType);
	}

	@Named("getLightningTimingType")
	static LightningTimingType getLightningTimingType(String timingType) {
		return LightningTimingType.valueOf(timingType);
	}
}
