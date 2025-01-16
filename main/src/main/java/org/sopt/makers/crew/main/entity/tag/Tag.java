package org.sopt.makers.crew.main.entity.tag;

import java.util.List;

import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;
import org.sopt.makers.crew.main.entity.tag.enums.TagType;
import org.sopt.makers.crew.main.entity.tag.enums.WelcomeMessageType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
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
	 * @implNote : 모임태그일 경우, lightningId == null
	 * @implNote : 번쩍태그일 경우, meetingId == null
	 * */
	@Enumerated(EnumType.STRING)
	@Column(name = "tagType")
	@NotNull
	private TagType tagType;

	@Column(name = "meetingId")
	private Integer meetingId;

	@Column(name = "lightningId")
	private Integer lightningId;

	@Enumerated(EnumType.STRING)
	@Column(name = "welcomeMessageTypes")
	@NotNull
	private List<WelcomeMessageType> welcomeMessageTypes;
}
