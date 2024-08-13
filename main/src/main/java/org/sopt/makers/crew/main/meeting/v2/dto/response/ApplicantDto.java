package org.sopt.makers.crew.main.meeting.v2.dto.response;

import com.querydsl.core.annotations.QueryProjection;

import java.util.Comparator;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import org.sopt.makers.crew.main.common.util.UserUtil;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;

@Getter
@Schema(name = "ApplicantDto", description = "모임 신청자 객체 Dto")
public class ApplicantDto {

	@Schema(description = "신청자 id, 크루에서 사용하는 userId", example = "1")
	@NotNull
	private final Integer id;

	@Schema(description = "신청자 이름", example = "송민규")
	@NotNull
	private final String name;

	@Schema(description = "신청자 org id, 메이커스 프로덕트에서 범용적으로 사용하는 userId", example = "1")
	@NotNull
	private final Integer orgId;

	@Schema(description = "신청자 기수 정보", example = "[{\"part\": \"웹\", \"generation\": 32}]")
	@NotNull
	private final UserActivityVO recentActivity;

	@Schema(description = "신청자 프로필 사진", example = "[url] 형식")
	private final String profileImage;

	@Schema(description = "신청자 핸드폰 번호", example = "010-1234-5678")
	private final String phone;

	@QueryProjection
	public ApplicantDto(Integer id, String name, Integer orgId, List<UserActivityVO> userActivityVOs,
		String profileImage,
		String phone) {

		this.id = id;
		this.name = name;
		this.orgId = orgId;
		this.recentActivity = UserUtil.getRecentUserActivity(userActivityVOs);
		this.profileImage = profileImage;
		this.phone = phone;
	}
}
