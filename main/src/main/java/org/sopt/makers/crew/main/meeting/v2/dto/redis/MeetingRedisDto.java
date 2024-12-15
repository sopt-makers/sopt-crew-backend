package org.sopt.makers.crew.main.meeting.v2.dto.redis;

import java.time.LocalDateTime;
import java.util.List;

import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import lombok.Getter;

@Getter
public class MeetingRedisDto {
	private final Integer id;
	private final Integer userId;
	private final String title;
	private final MeetingCategory category;
	private final List<ImageUrlVO> imageURL;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private final LocalDateTime startDate;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private final LocalDateTime endDate;

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
	private final Integer createdGeneration;
	private final Integer targetActiveGeneration;
	private final MeetingJoinablePart[] joinableParts;

	public Meeting toEntity() {
		return new Meeting(null, userId, title, category, imageURL, startDate, endDate, capacity, desc, processDesc,
			mStartDate, mEndDate, leaderDesc, null, note, isMentorNeeded, canJoinOnlyActiveGeneration,
			createdGeneration, targetActiveGeneration, joinableParts);
	}

	public static MeetingRedisDto of(Meeting meeting) {
		return new MeetingRedisDto(meeting.getId(), meeting.getUserId(), meeting.getTitle(), meeting.getCategory(),
			meeting.getImageURL(), meeting.getStartDate(), meeting.getEndDate(), meeting.getCapacity(),
			meeting.getDesc(), meeting.getProcessDesc(), meeting.getmStartDate(), meeting.getmEndDate(),
			meeting.getLeaderDesc(), meeting.getNote(), meeting.getIsMentorNeeded(),
			meeting.getCanJoinOnlyActiveGeneration(), meeting.getCreatedGeneration(),
			meeting.getTargetActiveGeneration(), meeting.getJoinableParts());
	}

	@JsonCreator
	public MeetingRedisDto(
		@JsonProperty("id") Integer id,
		@JsonProperty("userId") Integer userId,
		@JsonProperty("title") String title,
		@JsonProperty("category") MeetingCategory category,
		@JsonProperty("imageURL") List<ImageUrlVO> imageURL,
		@JsonProperty("startDate") LocalDateTime startDate,
		@JsonProperty("endDate") LocalDateTime endDate,
		@JsonProperty("capacity") Integer capacity,
		@JsonProperty("desc") String desc,
		@JsonProperty("processDesc") String processDesc,
		@JsonProperty("mStartDate") LocalDateTime mStartDate,
		@JsonProperty("mEndDate") LocalDateTime mEndDate,
		@JsonProperty("leaderDesc") String leaderDesc,
		@JsonProperty("note") String note,
		@JsonProperty("isMentorNeeded") Boolean isMentorNeeded,
		@JsonProperty("canJoinOnlyActiveGeneration") Boolean canJoinOnlyActiveGeneration,
		@JsonProperty("createdGeneration") Integer createdGeneration,
		@JsonProperty("targetActiveGeneration") Integer targetActiveGeneration,
		@JsonProperty("joinableParts") MeetingJoinablePart[] joinableParts) {
		this.id = id;
		this.userId = userId;
		this.title = title;
		this.category = category;
		this.imageURL = imageURL;
		this.startDate = startDate;
		this.endDate = endDate;
		this.capacity = capacity;
		this.desc = desc;
		this.processDesc = processDesc;
		this.mStartDate = mStartDate;
		this.mEndDate = mEndDate;
		this.leaderDesc = leaderDesc;
		this.note = note;
		this.isMentorNeeded = isMentorNeeded;
		this.canJoinOnlyActiveGeneration = canJoinOnlyActiveGeneration;
		this.createdGeneration = createdGeneration;
		this.targetActiveGeneration = targetActiveGeneration;
		this.joinableParts = joinableParts;
	}

	public LocalDateTime getmStartDate() {
		return mStartDate;
	}

	public LocalDateTime getmEndDate() {
		return mEndDate;
	}
}
