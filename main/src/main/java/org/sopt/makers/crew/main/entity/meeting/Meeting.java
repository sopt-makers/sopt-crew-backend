package org.sopt.makers.crew.main.entity.meeting;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;
import org.sopt.makers.crew.main.entity.meeting.converter.MeetingCategoryConverter;
import org.sopt.makers.crew.main.entity.meeting.enums.EnMeetingStatus;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.ForbiddenException;

import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import io.hypersistence.utils.hibernate.type.array.internal.AbstractArrayType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "meeting")
public class Meeting extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	/**
	 * 개설한 유저
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userId", nullable = false)
	private User user;

	/**
	 * 유저 id
	 */
	@Column(insertable = false, updatable = false)
	private Integer userId;

	/**
	 * 모임 제목
	 */
	@Column(nullable = false)
	private String title;

	/**
	 * 모임 카테고리
	 */
	@Column(name = "category", nullable = false)
	@Convert(converter = MeetingCategoryConverter.class)
	private MeetingCategory category;

	/**
	 * 이미지
	 */
	@Column(name = "imageURL", columnDefinition = "jsonb")
	@Type(JsonBinaryType.class)
	@Size(min = 1, max = 6)
	private List<ImageUrlVO> imageURL;

	/**
	 * 모집 시작 기간
	 */
	@Column(name = "startDate", nullable = false, columnDefinition = "TIMESTAMP")
	private LocalDateTime startDate;

	/**
	 * 모집 마감 기간
	 */
	@Column(name = "endDate", nullable = false, columnDefinition = "TIMESTAMP")
	private LocalDateTime endDate;

	/**
	 * 모집 인원
	 */
	@Column(name = "capacity", nullable = false)
	private Integer capacity;

	/**
	 * 모임 소개
	 */
	@Column(name = "desc", nullable = false)
	private String desc;

	/**
	 * 진행방식 소개
	 */
	@Column(name = "processDesc", nullable = false)
	private String processDesc;

	/**
	 * 모임 시작 기간
	 */
	@Column(name = "mStartDate", nullable = false, columnDefinition = "TIMESTAMP")
	private LocalDateTime mStartDate;

	/**
	 * 모임 마감 기간
	 */
	@Column(name = "mEndDate", nullable = false, columnDefinition = "TIMESTAMP")
	private LocalDateTime mEndDate;

	/**
	 * 개설자 소개
	 */
	@Column(name = "leaderDesc")
	private String leaderDesc;

	/**
	 * 유의 사항
	 */
	@Column(name = "note")
	private String note;

	/**
	 * 멘토 필요 여부
	 */
	@Column(name = "isMentorNeeded", nullable = false)
	private Boolean isMentorNeeded;

	/**
	 * 활동 기수만 참여 가능한지 여부
	 */
	@Column(name = "canJoinOnlyActiveGeneration", nullable = false)
	private Boolean canJoinOnlyActiveGeneration;

	/**
	 * 모임 기수
	 */
	@Column(name = "createdGeneration", nullable = false)
	private int createdGeneration;

	/**
	 * 대상 활동 기수
	 */
	@Column(name = "targetActiveGeneration")
	private Integer targetActiveGeneration;

	/**
	 * 모임 참여 가능한 파트
	 */
	@Type(value = EnumArrayType.class,
		parameters = @Parameter(name = AbstractArrayType.SQL_ARRAY_TYPE,
			value = "meeting_joinableparts_enum"))
	@Column(name = "joinableParts", columnDefinition = "meeting_joinableparts_enum[]")
	private MeetingJoinablePart[] joinableParts;

	@Builder
	public Meeting(User user, Integer userId, String title, MeetingCategory category,
		List<ImageUrlVO> imageURL, LocalDateTime startDate, LocalDateTime endDate, Integer capacity,
		String desc, String processDesc, LocalDateTime mStartDate, LocalDateTime mEndDate,
		String leaderDesc, String targetDesc, String note, Boolean isMentorNeeded,
		Boolean canJoinOnlyActiveGeneration, Integer createdGeneration,
		Integer targetActiveGeneration, MeetingJoinablePart[] joinableParts) {
		this.user = user;
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

	public static Meeting createFlashMeeting(User user, Integer userId, String title,
		List<ImageUrlVO> imageURL, LocalDateTime startDate,
		LocalDateTime endDate, Integer capacity, String desc,
		LocalDateTime mStartDate, LocalDateTime mEndDate,
		int createdGeneration, String processDesc, String leaderDesc, String note) {

		return Meeting.builder()
			.user(user)
			.userId(userId)
			.title(title)
			.category(MeetingCategory.FLASH)
			.imageURL(imageURL)
			.startDate(startDate)
			.endDate(endDate)
			.capacity(capacity)
			.desc(desc)
			.mStartDate(mStartDate)
			.mEndDate(mEndDate)
			.createdGeneration(createdGeneration)
			.processDesc(processDesc)
			.leaderDesc(leaderDesc)
			.note(note)
			.isMentorNeeded(false)
			.canJoinOnlyActiveGeneration(false)
			.targetActiveGeneration(null)
			.joinableParts(MeetingJoinablePart.values())
			.build();
	}

	/**
	 * 모임 모집상태 확인
	 *
	 * @return 모임 모집상태
	 */
	public Integer getMeetingStatusValue(LocalDateTime now) {
		return getMeetingStatus(now).getValue();
	}

	public EnMeetingStatus getMeetingStatus(LocalDateTime now) {
		if (now.isBefore(startDate)) {
			return EnMeetingStatus.BEFORE_START;
		} else if (now.isBefore(endDate)) {
			return EnMeetingStatus.APPLY_ABLE;
		} else {
			return EnMeetingStatus.RECRUITMENT_COMPLETE;
		}
	}

	public void validateMeetingCreator(Integer requestUserId) {
		if (Boolean.FALSE.equals(checkMeetingLeader(requestUserId))) {
			throw new ForbiddenException(FORBIDDEN_EXCEPTION.getErrorCode());
		}
	}

	public void validateIsNotMeetingLeader(Integer requestUserId) {
		if (checkMeetingLeader(requestUserId)) {
			throw new BadRequestException(LEADER_CANNOT_APPLY.getErrorCode());
		}
	}

	public Boolean checkMeetingLeader(Integer userId) {
		return this.userId.equals(userId);
	}

	public void validateCapacity(int approvedCount) {
		if (approvedCount >= this.capacity) {
			throw new BadRequestException(FULL_MEETING_CAPACITY.getErrorCode());
		}
	}

	public void updateMeeting(Meeting updateMeeting) {

		this.title = updateMeeting.getTitle();
		this.category = updateMeeting.getCategory();
		this.imageURL = updateMeeting.getImageURL();
		this.startDate = updateMeeting.getStartDate();
		this.endDate = updateMeeting.getEndDate();
		this.capacity = updateMeeting.getCapacity();
		this.desc = updateMeeting.getDesc();
		this.processDesc = updateMeeting.getProcessDesc();
		this.mStartDate = updateMeeting.getmStartDate();
		this.mEndDate = updateMeeting.getmEndDate();
		this.leaderDesc = updateMeeting.getLeaderDesc();
		this.note = updateMeeting.getNote();
		this.isMentorNeeded = updateMeeting.getIsMentorNeeded();
		this.canJoinOnlyActiveGeneration = updateMeeting.getCanJoinOnlyActiveGeneration();
		this.targetActiveGeneration = updateMeeting.getTargetActiveGeneration();
		this.joinableParts = updateMeeting.getJoinableParts();
	}

	public LocalDateTime getmStartDate() {
		return mStartDate;
	}

	public LocalDateTime getmEndDate() {
		return mEndDate;
	}

}
