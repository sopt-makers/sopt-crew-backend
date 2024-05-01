package org.sopt.makers.crew.main.meeting.v2.dto;

import static org.sopt.makers.crew.main.common.constant.CrewConst.DAY_END_TIME;
import static org.sopt.makers.crew.main.common.constant.CrewConst.DAY_START_TIME;
import static org.sopt.makers.crew.main.common.constant.CrewConst.DAY_TIME_FORMAT;

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
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2CreateMeetingBodyDto;

@Mapper(componentModel = "spring")
public interface MeetingMapper {

    @Mapping(source = "requestBody.files", target = "imageURL", qualifiedByName = "getImageURL")
    @Mapping(source = "requestBody.category", target = "category", qualifiedByName = "getCategory")
    @Mapping(source = "requestBody.startDate", target = "startDate", qualifiedByName = "getStartDate")
    @Mapping(source = "requestBody.endDate", target = "endDate", qualifiedByName = "getEndDate")
    @Mapping(source = "requestBody.mStartDate", target = "mStartDate", qualifiedByName = "getStartDate")
    @Mapping(source = "requestBody.mEndDate", target = "mEndDate", qualifiedByName = "getEndDate")
    Meeting toMeetingEntity(MeetingV2CreateMeetingBodyDto requestBody, Integer targetActiveGeneration,
                            Integer createdGeneration, User user, Integer userId);

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
}
