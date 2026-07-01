package org.sopt.makers.crew.main.meetingdemand.v2.service;

import static org.sopt.makers.crew.main.entity.meetingdemand.enums.MeetingDemandStatus.BEFORE_OPEN;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.ALREADY_REPORTED_MEETING_DEMAND;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.FORBIDDEN_EXCEPTION;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemand;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandRepository;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandWait;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandWaitRepository;
import org.sopt.makers.crew.main.entity.report.Report;
import org.sopt.makers.crew.main.entity.report.ReportRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.ForbiddenException;
import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;
import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.query.MeetingDemandV2GetMeetingDemandsQueryDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.query.MeetingDemandV2GetOpenedMeetingsQueryDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.request.MeetingDemandV2CreateMeetingDemandBodyDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2CreateMeetingDemandResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2GetMeetingDemandResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2GetMeetingDemandsResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2GetOpenedMeetingResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2GetOpenedMeetingsResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2ReportResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2SwitchMeetingDemandWaitResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingDemandV2ServiceImpl implements MeetingDemandV2Service {

	private final UserRepository userRepository;
	private final MeetingDemandRepository meetingDemandRepository;
	private final MeetingDemandWaitRepository meetingDemandWaitRepository;
	private final MeetingRepository meetingRepository;
	private final ReportRepository reportRepository;
	private final MeetingDemandFactory meetingDemandFactory;
	private final MeetingDemandPageNormalizer meetingDemandPageNormalizer;

	@Override
	public MeetingDemandV2GetMeetingDemandsResponseDto getMeetingDemands(
		MeetingDemandV2GetMeetingDemandsQueryDto queryDto, Integer userId) {

		int totalCount = meetingDemandRepository.countByStatus(BEFORE_OPEN);
		PageOptionsDto effectiveQueryDto = meetingDemandPageNormalizer.normalize(queryDto, totalCount);
		Page<MeetingDemand> meetingDemands = meetingDemandRepository.findAllByStatus(BEFORE_OPEN,
			PageRequest.of(
				effectiveQueryDto.getPage() - 1,
				effectiveQueryDto.getTake(),
				Sort.by(Sort.Order.desc("createdTimestamp"), Sort.Order.desc("id"))
			));

		Set<Integer> waitingMeetingDemandIds = getWaitingMeetingDemandIds(meetingDemands.getContent(), userId);
		List<MeetingDemandV2GetMeetingDemandResponseDto> responseDtos = meetingDemands.getContent().stream()
			.map(meetingDemand -> MeetingDemandV2GetMeetingDemandResponseDto.of(
				meetingDemand,
				waitingMeetingDemandIds.contains(meetingDemand.getId()),
				meetingDemand.isWriter(userId),
				meetingRepository.countByMeetingDemandId(meetingDemand.getId())
			))
			.toList();

		PageMetaDto pageMetaDto = new PageMetaDto(effectiveQueryDto, totalCount);

		return MeetingDemandV2GetMeetingDemandsResponseDto.of(responseDtos, pageMetaDto);
	}

	@Override
	public MeetingDemandV2GetMeetingDemandResponseDto getMeetingDemand(Integer meetingDemandId, Integer userId) {
		MeetingDemand meetingDemand = meetingDemandRepository.findByIdOrThrow(meetingDemandId);
		boolean isWaiting = meetingDemandWaitRepository.existsByMeetingDemandIdAndUserId(meetingDemandId, userId);
		int openedMeetingCount = meetingRepository.countByMeetingDemandId(meetingDemandId);

		return MeetingDemandV2GetMeetingDemandResponseDto.of(meetingDemand, isWaiting, meetingDemand.isWriter(userId),
			openedMeetingCount);
	}

	@Override
	public MeetingDemandV2GetOpenedMeetingsResponseDto getOpenedMeetings(
		Integer meetingDemandId, MeetingDemandV2GetOpenedMeetingsQueryDto queryDto) {
		meetingDemandRepository.findByIdOrThrow(meetingDemandId);

		int totalCount = meetingRepository.countByMeetingDemandId(meetingDemandId);
		PageOptionsDto effectiveQueryDto = meetingDemandPageNormalizer.normalize(queryDto, totalCount);
		Page<Meeting> meetings = meetingRepository.findAllByMeetingDemandId(meetingDemandId,
			PageRequest.of(
				effectiveQueryDto.getPage() - 1,
				effectiveQueryDto.getTake(),
				Sort.by(Sort.Order.desc("createdTimestamp"), Sort.Order.desc("id"))
			));

		Map<Integer, User> userMap = getUsersById(meetings.getContent());
		List<MeetingDemandV2GetOpenedMeetingResponseDto> responseDtos = meetings.getContent().stream()
			.map(meeting -> MeetingDemandV2GetOpenedMeetingResponseDto.of(meeting, userMap.get(meeting.getUserId())))
			.toList();
		PageMetaDto pageMetaDto = new PageMetaDto(effectiveQueryDto, totalCount);

		return MeetingDemandV2GetOpenedMeetingsResponseDto.of(totalCount, responseDtos, pageMetaDto);
	}

	@Override
	@Transactional
	public MeetingDemandV2CreateMeetingDemandResponseDto createMeetingDemand(
		MeetingDemandV2CreateMeetingDemandBodyDto requestBody, Integer userId) {
		User user = userRepository.findByIdOrThrow(userId);
		MeetingDemand meetingDemand = meetingDemandFactory.create(user, requestBody);
		MeetingDemand savedMeetingDemand = meetingDemandRepository.save(meetingDemand);

		return MeetingDemandV2CreateMeetingDemandResponseDto.of(savedMeetingDemand.getId());
	}

	@Override
	@Transactional
	public void deleteMeetingDemand(Integer meetingDemandId, Integer userId) {
		MeetingDemand meetingDemand = meetingDemandRepository.findByIdWithPessimisticWriteLockOrThrow(meetingDemandId);

		meetingDemand.validateWriter(userId);
		meetingDemand.validateBeforeOpen();

		meetingDemandWaitRepository.deleteAllByMeetingDemandId(meetingDemandId);
		meetingDemandRepository.delete(meetingDemand);
	}

	@Override
	@Transactional
	public MeetingDemandV2SwitchMeetingDemandWaitResponseDto switchMeetingDemandWait(
		Integer meetingDemandId, Integer userId) {
		MeetingDemand meetingDemand = meetingDemandRepository.findByIdWithPessimisticWriteLockOrThrow(meetingDemandId);

		meetingDemand.validateNotWriter(userId);

		boolean isWaiting = meetingDemandWaitRepository.existsByMeetingDemandIdAndUserId(meetingDemandId, userId);
		if (isWaiting) {
			meetingDemandWaitRepository.deleteByMeetingDemandIdAndUserId(meetingDemandId, userId);
		} else {
			MeetingDemandWait meetingDemandWait = MeetingDemandWait.builder()
				.meetingDemandId(meetingDemandId)
				.userId(userId)
				.build();

			meetingDemandWaitRepository.save(meetingDemandWait);
		}

		meetingDemandWaitRepository.flush();
		long waitCount = meetingDemandWaitRepository.countByMeetingDemandId(meetingDemandId);
		meetingDemand.syncWaitCount((int)waitCount);

		return MeetingDemandV2SwitchMeetingDemandWaitResponseDto.of(meetingDemand.getWaitCount(), !isWaiting);
	}

	@Override
	@Transactional
	public MeetingDemandV2ReportResponseDto reportMeetingDemand(Integer meetingDemandId, Integer userId) {
		MeetingDemand meetingDemand = meetingDemandRepository.findByIdOrThrow(meetingDemandId);

		if (meetingDemand.isWriter(userId)) {
			throw new ForbiddenException(FORBIDDEN_EXCEPTION.getErrorCode());
		}
		if (reportRepository.existsByMeetingDemandIdAndUserId(meetingDemandId, userId)) {
			throw new BadRequestException(ALREADY_REPORTED_MEETING_DEMAND.getErrorCode());
		}

		Report report = Report.builder()
			.meetingDemand(meetingDemand)
			.meetingDemandId(meetingDemandId)
			.userId(userId)
			.build();

		Report savedReport = reportRepository.save(report);

		return MeetingDemandV2ReportResponseDto.of(savedReport.getId());
	}

	private Set<Integer> getWaitingMeetingDemandIds(List<MeetingDemand> meetingDemands, Integer userId) {
		if (meetingDemands.isEmpty()) {
			return Set.of();
		}

		List<Integer> meetingDemandIds = meetingDemands.stream()
			.map(MeetingDemand::getId)
			.toList();

		return meetingDemandWaitRepository.findAllByMeetingDemandIdInAndUserId(meetingDemandIds, userId).stream()
			.map(MeetingDemandWait::getMeetingDemandId)
			.collect(Collectors.toSet());
	}

	private Map<Integer, User> getUsersById(List<Meeting> meetings) {
		if (meetings.isEmpty()) {
			return Map.of();
		}

		List<Integer> userIds = meetings.stream()
			.map(Meeting::getUserId)
			.distinct()
			.toList();

		return userRepository.findAllById(userIds).stream()
			.collect(Collectors.toMap(User::getId, user -> user));
	}
}
