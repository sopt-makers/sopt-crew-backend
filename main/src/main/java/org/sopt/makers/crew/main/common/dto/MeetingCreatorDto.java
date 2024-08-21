package org.sopt.makers.crew.main.common.dto;

import org.sopt.makers.crew.main.entity.user.User;

import com.querydsl.core.annotations.QueryProjection;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Schema(name = "MeetingCreatorDto", description = "모임 개설자 Dto")
@Getter
public class MeetingCreatorDto {
	@Schema(description = "모임장 id, 크루에서 사용하는 userId", example = "1")
	@NotNull
	Integer id;
	@Schema(description = "모임장 이름", example = "홍길동")
	@NotNull
	String name;
	@Schema(description = "모임장 org id, 메이커스 프로덕트에서 범용적으로 사용하는 userId", example = "1")
	@NotNull
	Integer orgId;
	@Schema(description = "모임장 프로필 사진", example = "[url] 형식")
	String profileImage;

	@QueryProjection
	public MeetingCreatorDto(Integer id, String name, Integer orgId, String profileImag) {
		this.id = id;
		this.name = name;
		this.orgId = orgId;
		this.profileImage = profileImag;
	}

	public static MeetingCreatorDto of(User user) {
		return new MeetingCreatorDto(user.getId(), user.getName(), user.getOrgId(), user.getProfileImage());
	}

}
