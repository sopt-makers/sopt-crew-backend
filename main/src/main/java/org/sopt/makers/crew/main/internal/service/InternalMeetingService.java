package org.sopt.makers.crew.main.internal.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.meeting.CoLeaderRepository;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.external.playground.service.MemberBlockService;
import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;
import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.global.util.ActiveGenerationProvider;
import org.sopt.makers.crew.main.global.util.CustomPageable;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.internal.dto.InternalMeetingForWritingPostDto;
import org.sopt.makers.crew.main.internal.dto.InternalMeetingGetAllMeetingDto;
import org.sopt.makers.crew.main.internal.dto.InternalMeetingGetAllWritingPostResponseDto;
import org.sopt.makers.crew.main.internal.dto.InternalMeetingResponseDto;
import org.sopt.makers.crew.main.internal.dto.UserAppliedMeetingDto;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

	private final ActiveGenerationProvider activeGenerationProvider;
	private final Time time;
	private final CoLeaderRepository coLeaderRepository;
	private final ApplyRepository applyRepository;

	/**
	 * [for. APP BE] 모일 리스트 페이지네이션 조회 (10개)
	 *
	 * @param queryCommand 게시글 조회를 위한 쿼리 명령 객체
	 * @return 게시글 정보(게시글 객체 + 댓글 단 사람의 썸네일 + 차단된 유저의 게시물 여부)와 페이지 메타 정보를 포함한 응답 DTO
	 * @apiNote 사용자가 차단한 유저의 모임은 해당 모임에 대한 차단 여부를 함께 반환
	 * id -> playgroundId
	 */
	public InternalMeetingGetAllMeetingDto getMeetings(
		MeetingV2GetAllMeetingQueryDto queryCommand, Integer orgId) {
		Sort sort = Sort.by(Sort.Direction.ASC, "id");

		Page<Meeting> meetings = meetingRepository.findAllByQuery(queryCommand,
			new CustomPageable(queryCommand.getPage() - 1, queryCommand.getTake(), sort), time,
			activeGenerationProvider.getActiveGeneration());

		List<Long> userOrgIds = meetings.getContent()
			.stream()
			.map(meeting -> meeting.getUser().getId().longValue())
			.toList();

		Map<Long, Boolean> blockedUsers = memberBlockService.getBlockedUsers(orgId.longValue(), userOrgIds);

		List<InternalMeetingResponseDto> meetingResponseDtos = meetings.getContent().stream()
			.map(meeting -> InternalMeetingResponseDto.of(meeting, time.now(),
				blockedUsers.getOrDefault(meeting.getUserId().longValue(), false)))
			.toList();

		PageOptionsDto pageOptionsDto = new PageOptionsDto(meetings.getPageable().getPageNumber() + 1,
			meetings.getPageable().getPageSize());
		PageMetaDto pageMetaDto = new PageMetaDto(pageOptionsDto, (int)meetings.getTotalElements());

		return new InternalMeetingGetAllMeetingDto(meetingResponseDtos, pageMetaDto);
	}

	/**
	 * [for. PG BE] 모임 리스트 페이지네이션 조회 (10개)
	 *
	 * @param pageDto 페이지네이션 정보
	 * @return 게시글 정보(게시글 객체 + 댓글 단 사람의 썸네일 + 차단된 유저의 게시물 여부)와 페이지 메타 정보를 포함한 응답 DTO
	 */
	public InternalMeetingGetAllWritingPostResponseDto getMeetingsForWritingPost(PageOptionsDto pageDto) {

		Page<Meeting> pagedMeetings = meetingRepository.findAllByQuery(
			PageRequest.of(pageDto.getPage() - 1, pageDto.getTake()));

		List<InternalMeetingForWritingPostDto> meetings = pagedMeetings.getContent().stream()
			.map(InternalMeetingForWritingPostDto::from)
			.toList();

		PageMetaDto pageMetaDto = new PageMetaDto(pageDto, (int)pagedMeetings.getTotalElements());

		return InternalMeetingGetAllWritingPostResponseDto.from(meetings, pageMetaDto);
	}

	public List<UserAppliedMeetingDto> getAppliedMeetingInfo(Integer userId) {

		List<Meeting> myMeetings = meetingRepository.findAllByUserId(userId);
		List<Apply> allByUserIdAndStatus = applyRepository.findAllByUserIdAndStatus(userId, EnApplyStatus.APPROVE);

		return Stream.concat(myMeetings.stream(),
				allByUserIdAndStatus.stream().map(Apply::getMeeting))
			.map(meeting -> {
				String title = meeting.getTitle();
				String imgUrl = meeting.getImageURL() == null || meeting.getImageURL().isEmpty() ? null :
					meeting.getImageURL().get(0).getUrl();
				LocalDateTime meetingStartTIme = meeting.getmStartDate();
				LocalDateTime meetingEndTIme = meeting.getmEndDate();
				boolean isUserCoLeader = coLeaderRepository.existsByMeetingIdAndUserId(meeting.getId(), userId);
				String category = meeting.getCategory().getValue();
				return UserAppliedMeetingDto.of(category, title, meetingStartTIme, meetingEndTIme,
					isUserCoLeader, imgUrl);
			}).toList();
	}
}
