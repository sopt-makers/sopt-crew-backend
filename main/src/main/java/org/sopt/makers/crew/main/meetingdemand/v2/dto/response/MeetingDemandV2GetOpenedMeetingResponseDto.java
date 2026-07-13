package org.sopt.makers.crew.main.meetingdemand.v2.dto.response;

import java.util.List;

import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.user.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "MeetingDemandV2GetOpenedMeetingResponseDto", description = "모임 수요 기반 개설 모임 카드 응답 Dto")
public record MeetingDemandV2GetOpenedMeetingResponseDto(
	@Schema(description = "모임 id", example = "1")
	@NotNull
	Integer meetingId,

	@Schema(description = "모임 제목", example = "러닝 크루 스터디를 원해요!")
	@NotNull
	String title,

	@Schema(description = "모임 대표 사진 URL", example = "https://example.com/image.png")
	String imageUrl,

	@Schema(description = "모임 카테고리", example = "스터디")
	@NotNull
	String category,

	@Schema(description = "모임 개설자")
	@NotNull
	MeetingDemandV2OpenedMeetingCreatorResponseDto user
) {
	public static MeetingDemandV2GetOpenedMeetingResponseDto of(Meeting meeting, User user) {
		return new MeetingDemandV2GetOpenedMeetingResponseDto(
			meeting.getId(),
			meeting.getTitle(),
			getFirstImageUrl(meeting.getImageURL()),
			meeting.getCategory().getValue(),
			MeetingDemandV2OpenedMeetingCreatorResponseDto.of(user)
		);
	}

	private static String getFirstImageUrl(List<ImageUrlVO> imageURL) {
		if (imageURL == null || imageURL.isEmpty()) {
			return null;
		}

		return imageURL.get(0).getUrl();
	}
}
