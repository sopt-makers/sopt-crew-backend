package org.sopt.makers.crew.main.entity.tag;

import java.util.List;

import org.hibernate.annotations.Type;
import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;
import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;
import org.sopt.makers.crew.main.entity.tag.enums.TagType;
import org.sopt.makers.crew.main.entity.tag.enums.WelcomeMessageType;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tag")
public class Tag extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	/**
	 * @implSpec : 모임태그 or 번쩍태그 구분
	 * @implNote : 모임태그일 경우, flashId == null
	 * @implNote : 번쩍태그일 경우, meetingId == null
	 * */
	@Enumerated(EnumType.STRING)
	@Column(name = "tagType")
	@NotNull
	private TagType tagType;

	@Column(name = "meetingId")
	private Integer meetingId;

	@Column(name = "flashId")
	private Integer flashId;

	@Column(name = "welcomeMessageTypes", columnDefinition = "jsonb")
	@Type(JsonBinaryType.class)
	private List<WelcomeMessageType> welcomeMessageTypes;

	@Column(name = "meetingKeywordTypes", columnDefinition = "jsonb")
	@Type(JsonBinaryType.class)
	@Size(min = 1, max = 2)
	private List<MeetingKeywordType> meetingKeywordTypes;

	@Builder
	private Tag(TagType tagType, Integer meetingId, Integer flashId,
		List<WelcomeMessageType> welcomeMessageTypes,
		List<MeetingKeywordType> meetingKeywordTypes) {
		this.tagType = tagType;
		this.meetingId = meetingId;
		this.flashId = flashId;
		this.welcomeMessageTypes = welcomeMessageTypes;
		this.meetingKeywordTypes = meetingKeywordTypes;
	}

	public static Tag createGeneralMeetingTag(TagType tagType, Integer meetingId,
		List<WelcomeMessageType> welcomeMessageTypes,
		List<MeetingKeywordType> meetingKeywordTypes) {
		return Tag.builder()
			.tagType(tagType)
			.meetingId(meetingId)
			.flashId(null)
			.welcomeMessageTypes(welcomeMessageTypes)
			.meetingKeywordTypes(meetingKeywordTypes)
			.build();
	}

	public static Tag createFlashMeetingTag(TagType tagType, Integer flashId,
		List<WelcomeMessageType> welcomeMessageTypes,
		List<MeetingKeywordType> meetingKeywordTypes) {
		return Tag.builder()
			.tagType(tagType)
			.meetingId(null)
			.flashId(flashId)
			.welcomeMessageTypes(welcomeMessageTypes)
			.meetingKeywordTypes(meetingKeywordTypes)
			.build();
	}

	public void updateWelcomeMessageTypes(List<WelcomeMessageType> newWelcomeMessageTypes) {
		this.welcomeMessageTypes = newWelcomeMessageTypes;
	}
}
