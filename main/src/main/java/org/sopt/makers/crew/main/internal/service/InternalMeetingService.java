package org.sopt.makers.crew.main.internal.service;

import java.util.List;
import java.util.Map;

import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.external.playground.service.MemberBlockService;
import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;
import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.global.util.CustomPageable;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.internal.dto.InternalMeetingGetAllMeetingDto;
import org.sopt.makers.crew.main.internal.dto.InternalMeetingResponseDto;
import org.sopt.makers.crew.main.internal.dto.UserOrgIdRequestDto;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternalMeetingService {
	private final MeetingRepository meetingRepository;
	private final MemberBlockService memberBlockService;

	private final Time time;

	/**
	 * [for. APP BE] 모일 리스트 페이지네이션 조회 (10개)
	 *
	 * @param queryCommand 게시글 조회를 위한 쿼리 명령 객체
	 * @param orgIdRequestDto 플레이그라운드 orgId
	 * @return 게시글 정보(게시글 객체 + 댓글 단 사람의 썸네일 + 차단된 유저의 게시물 여부)와 페이지 메타 정보를 포함한 응답 DTO
	 * @apiNote 사용자가 차단한 유저의 모임은 해당 모임에 대한 차단 여부를 함께 반환
	 */
	public InternalMeetingGetAllMeetingDto getMeetings(
		MeetingV2GetAllMeetingQueryDto queryCommand, UserOrgIdRequestDto orgIdRequestDto) {
		Sort sort = Sort.by(Sort.Direction.ASC, "id");

		Page<Meeting> meetings = meetingRepository.findAllByQuery(queryCommand,
			new CustomPageable(queryCommand.getPage() - 1, queryCommand.getTake(), sort), time);
		Map<Long, Boolean> blockedUsers = memberBlockService.getBlockedUsers(orgIdRequestDto.orgId().longValue());

		List<InternalMeetingResponseDto> meetingResponseDtos = meetings.getContent().stream()
			.map(meeting -> InternalMeetingResponseDto.of(meeting, time.now(),
				blockedUsers.getOrDefault(meeting.getUserId().longValue(), false)))
			.toList();

		PageOptionsDto pageOptionsDto = new PageOptionsDto(meetings.getPageable().getPageNumber() + 1,
			meetings.getPageable().getPageSize());
		PageMetaDto pageMetaDto = new PageMetaDto(pageOptionsDto, (int)meetings.getTotalElements());

		return new InternalMeetingGetAllMeetingDto(meetingResponseDtos, pageMetaDto);
	}
}
