package org.sopt.makers.crew.main.global.dto;

import java.util.List;

import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(name = "MeetingCreatorDto", description = "모임 개설자 Dto")
public class MeetingCreatorDto {
	@Schema(description = "모임장 id, 크루에서 사용하는 userId", example = "1")
	@NotNull
	private final Integer id;

	@Schema(description = "모임장 이름", example = "홍길동")
	@NotNull
	private final String name;

	@Schema(description = "모임장 org id, 메이커스 프로덕트에서 범용적으로 사용하는 userId", example = "1")
	@NotNull
	private final Integer orgId;

	@Schema(description = "모임장 프로필 사진", example = "[url] 형식")
	private final String profileImage;

	@Schema(description = "활동 기수", example = "")
	@NotNull
	private final List<UserActivityVO> activities;

	@Schema(description = "전화번호", example = "01094726796")
	@NotNull
	private final String phone;

	public static MeetingCreatorDto from(User user) {
		return new MeetingCreatorDto(user.getId(), user.getName(), user.getOrgId(), user.getProfileImage(),
			user.getActivities(), user.getPhone());
	}

	@JsonCreator // JSON 역직렬화를 위한 생성자
	public MeetingCreatorDto(
		@JsonProperty("id") Integer id,
		@JsonProperty("name") String name,
		@JsonProperty("orgId") Integer orgId,
		@JsonProperty("profileImage") String profileImage,
		@JsonProperty("activities") List<UserActivityVO> activities,
		@JsonProperty("phone") String phone
	) {
		this.id = id;
		this.name = name;
		this.orgId = orgId;
		this.profileImage = profileImage;
		this.activities = activities;
		this.phone = phone;
	}
}
