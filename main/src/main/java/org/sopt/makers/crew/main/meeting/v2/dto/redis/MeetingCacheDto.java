package org.sopt.makers.crew.main.meeting.v2.dto.redis;

import java.time.LocalDateTime;
import java.util.List;

import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.meeting.vo.MeetingJoinInfo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import lombok.Getter;

@Getter
public class MeetingCacheDto {
	private final Integer id;
	private final Integer userId;
	private final String title;
	private final String subTitle;
	private final MeetingCategory category;
	private final List<ImageUrlVO> imageURL;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private final LocalDateTime startDate;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private final LocalDateTime endDate;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private final LocalDateTime createdTimestamp;

	private final Integer capacity;
	private final String desc;
	private final String processDesc;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private final LocalDateTime mStartDate;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private final LocalDateTime mEndDate;

	private final String leaderDesc;
	private final String note;
	private final Boolean isMentorNeeded;
	private final Boolean canJoinOnlyActiveGeneration;
	private final MeetingJoinInfo joinInfo;
	private final Integer createdGeneration;
	private final Integer targetActiveGeneration;
	private final MeetingJoinablePart[] joinableParts;

	@JsonCreator
	public MeetingCacheDto(
		@JsonProperty("id") Integer id,
		@JsonProperty("userId") Integer userId,
		@JsonProperty("title") String title,
		@JsonProperty("subTitle") String subTitle,
		@JsonProperty("category") MeetingCategory category,
		@JsonProperty("imageURL") List<ImageUrlVO> imageURL,
		@JsonProperty("startDate") LocalDateTime startDate,
		@JsonProperty("endDate") LocalDateTime endDate,
		@JsonProperty("createdTimestamp") LocalDateTime createdTimestamp,
		@JsonProperty("capacity") Integer capacity,
		@JsonProperty("desc") String desc,
		@JsonProperty("processDesc") String processDesc,
		@JsonProperty("mStartDate") LocalDateTime mStartDate,
		@JsonProperty("mEndDate") LocalDateTime mEndDate,
		@JsonProperty("leaderDesc") String leaderDesc,
		@JsonProperty("note") String note,
		@JsonProperty("isMentorNeeded") Boolean isMentorNeeded,
		@JsonProperty("canJoinOnlyActiveGeneration") Boolean canJoinOnlyActiveGeneration,
		@JsonProperty("joinInfo") MeetingJoinInfo joinInfo,
		@JsonProperty("createdGeneration") Integer createdGeneration,
		@JsonProperty("targetActiveGeneration") Integer targetActiveGeneration,
		@JsonProperty("joinableParts") MeetingJoinablePart[] joinableParts) {
		this.id = id;
		this.userId = userId;
		this.title = title;
		this.subTitle = subTitle;
		this.category = category;
		this.imageURL = imageURL;
		this.startDate = startDate;
		this.endDate = endDate;
		this.createdTimestamp = createdTimestamp;
		this.capacity = capacity;
		this.desc = desc;
		this.processDesc = processDesc;
		this.mStartDate = mStartDate;
		this.mEndDate = mEndDate;
		this.leaderDesc = leaderDesc;
		this.note = note;
		this.isMentorNeeded = isMentorNeeded;
		this.canJoinOnlyActiveGeneration = canJoinOnlyActiveGeneration;
		this.joinInfo = joinInfo;
		this.createdGeneration = createdGeneration;
		this.targetActiveGeneration = targetActiveGeneration;
		this.joinableParts = joinableParts;
	}

	public static MeetingCacheDto from(Meeting meeting) {
		return new MeetingCacheDto(meeting.getId(), meeting.getUserId(), meeting.getTitle(), meeting.getSubTitle(),
			meeting.getCategory(),
			meeting.getImageURL(), meeting.getStartDate(), meeting.getEndDate(), meeting.createdTimestamp,
			meeting.getCapacity(), meeting.getDesc(), meeting.getProcessDesc(), meeting.getmStartDate(),
			meeting.getmEndDate(), meeting.getLeaderDesc(), meeting.getNote(), meeting.getIsMentorNeeded(),
			meeting.getCanJoinOnlyActiveGeneration(), meeting.getJoinInfo(), meeting.getCreatedGeneration(),
			meeting.getTargetActiveGeneration(), meeting.getJoinableParts());
	}

	public Meeting toEntity() {
		Meeting meeting = Meeting.builder()
			.userId(userId)
			.title(title)
			.subTitle(subTitle)
			.category(category)
			.imageURL(imageURL)
			.startDate(startDate)
			.endDate(endDate)
			.capacity(capacity)
			.desc(desc)
			.processDesc(processDesc)
			.mStartDate(mStartDate)
			.mEndDate(mEndDate)
			.leaderDesc(leaderDesc)
			.note(note)
			.isMentorNeeded(isMentorNeeded)
			.canJoinOnlyActiveGeneration(canJoinOnlyActiveGeneration)
			.joinInfo(joinInfo)
			.createdGeneration(createdGeneration)
			.targetActiveGeneration(targetActiveGeneration)
			.joinableParts(joinableParts)
			.build();

		meeting.createdTimestamp = createdTimestamp;
		return meeting;
	}

	public LocalDateTime getmStartDate() {
		return mStartDate;
	}

	public LocalDateTime getmEndDate() {
		return mEndDate;
	}
}
