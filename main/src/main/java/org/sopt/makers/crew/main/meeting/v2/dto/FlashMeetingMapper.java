package org.sopt.makers.crew.main.meeting.v2.dto;

import static org.sopt.makers.crew.main.global.constant.CrewConst.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.flash.v2.dto.request.FlashV2CreateAndUpdateFlashBodyWithoutWelcomeMessageDto;

@Mapper(componentModel = "spring")
public interface FlashMeetingMapper {
	@Mapping(source = "flashBody.title", target = "title")
	@Mapping(source = "flashBody.files", target = "imageURL", qualifiedByName = "getImageURL")
	@Mapping(source = "now", target = "startDate")
	@Mapping(source = "flashBody.activityStartDate", target = "endDate", qualifiedByName = "getPreviousDayEndTime")
	@Mapping(source = "flashBody.maximumCapacity", target = "capacity")
	@Mapping(source = "flashBody.desc", target = "desc")
	@Mapping(source = "flashBody.activityStartDate", target = "mStartDate", qualifiedByName = "getStartDate")
	@Mapping(source = "flashBody.activityEndDate", target = "mEndDate", qualifiedByName = "getEndDate")
	@Mapping(target = "category", expression = "java(org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory.FLASH)")
	@Mapping(target = "createdGeneration", expression = "java(org.sopt.makers.crew.main.global.constant.CrewConst.ACTIVE_GENERATION)")
	@Mapping(target = "processDesc", constant = "") // null 대신 빈 문자열로 NPE 방지
	@Mapping(target = "leaderDesc", constant = "") // null 대신 빈 문자열로 NPE 방지
	@Mapping(target = "note", constant = "") // null 대신 빈 문자열로 NPE 방지
	@Mapping(target = "isMentorNeeded", constant = "false") // 번쩍 정책에 맞게 false
	@Mapping(target = "canJoinOnlyActiveGeneration", constant = "false") // 번쩍 정책에 맞게 false
	@Mapping(target = "targetActiveGeneration", expression = "java(null)") // 번쩍 정책에 맞게 null
	@Mapping(target = "joinableParts", expression = "java(org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart.values())")
		// 번쩍 정책에 맞게 모든 파트 허용
	Meeting toMeetingEntityForFlash(FlashV2CreateAndUpdateFlashBodyWithoutWelcomeMessageDto flashBody,
		User user, Integer userId, LocalDateTime now);

	@Named("getImageURL")
	static List<ImageUrlVO> getImageURL(List<String> files) {
		AtomicInteger index = new AtomicInteger(0);

		return files.stream()
			.map(url -> new ImageUrlVO(index.getAndIncrement(), url))
			.toList();
	}

	@Named("getCategory")
	static MeetingCategory getCategory(String category) {
		return MeetingCategory.ofValue(category);
	}

	@Named("getStartDate")
	static LocalDateTime getStartDate(String date) {
		return LocalDateTime.parse(date + DAY_START_TIME, DateTimeFormatter.ofPattern(DAY_TIME_FORMAT));
	}

	@Named("getEndDate")
	static LocalDateTime getEndDate(String date) {
		return LocalDateTime.parse(date + DAY_END_TIME, DateTimeFormatter.ofPattern(DAY_TIME_FORMAT));

	}

	@Named("getPreviousDayEndTime")
	static LocalDateTime getPreviousDayEndTime(String date) {
		return LocalDate.parse(date, DateTimeFormatter.ofPattern(DAY_FORMAT))
			.minusDays(1)
			.atTime(DAY_END_HOUR, DAY_END_MINUTE, DAY_END_SECOND);
	}
}
