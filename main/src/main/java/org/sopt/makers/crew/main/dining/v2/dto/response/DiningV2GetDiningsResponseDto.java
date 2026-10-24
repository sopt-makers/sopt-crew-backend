package org.sopt.makers.crew.main.dining.v2.dto.response;

import java.time.LocalDate;
import java.util.List;

import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DiningV2GetDiningsResponseDto", description = "회식 메인 목록 응답 Dto")
public record DiningV2GetDiningsResponseDto(
	List<DiningCard> dinings,
	PageMetaDto meta
) {

	@Schema(name = "DiningV2DiningCardDto", description = "회식 카드")
	public record DiningCard(
		@Schema(description = "회식(모임) id", example = "1")
		Integer meetingId,

		@Schema(description = "회식명", example = "서버 파트 회식")
		String title,

		@Schema(description = "회식 일자", example = "2026-10-30")
		LocalDate diningDate,

		@Schema(description = "대표 이미지", example = "[url] 형식")
		String imageUrl,

		@Schema(description = "종료 여부. false 면 진행 중", example = "false")
		boolean isEnded,

		@Schema(description = "참여 파트", example = "[\"SERVER\", \"WEB\"]")
		MeetingJoinablePart[] joinableParts,

		@Schema(description = "전체 회차의 고유 참석자 수", example = "12")
		long attendeeCount,

		@Schema(description = "개설자")
		DiningV2LeaderDto leader,

		@Schema(description = "참석자 관리 버튼 노출 여부 (개설자 또는 승인된 공동 결제자)", example = "true")
		boolean canManageAttendees
	) {
	}
}
