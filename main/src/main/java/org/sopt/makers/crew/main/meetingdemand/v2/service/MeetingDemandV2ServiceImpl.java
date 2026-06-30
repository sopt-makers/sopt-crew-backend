package org.sopt.makers.crew.main.meetingdemand.v2.service;

import static org.sopt.makers.crew.main.entity.meetingdemand.enums.MeetingDemandStatus.BEFORE_OPEN;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.INVALID_MEETING_KEYWORD_SIZE;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemand;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandRepository;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandWait;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandWaitRepository;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.query.MeetingDemandV2GetMeetingDemandsQueryDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.request.MeetingDemandV2CreateMeetingDemandBodyDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2CreateMeetingDemandResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2GetMeetingDemandResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2GetMeetingDemandsResponseDto;
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

	private static final int MAX_MEETING_KEYWORD_SIZE = 2;

	private final UserRepository userRepository;
	private final MeetingDemandRepository meetingDemandRepository;
	private final MeetingDemandWaitRepository meetingDemandWaitRepository;
	private final MeetingRepository meetingRepository;

	@Override
	public MeetingDemandV2GetMeetingDemandsResponseDto getMeetingDemands(
		MeetingDemandV2GetMeetingDemandsQueryDto queryDto, Integer userId) {

		int totalCount = meetingDemandRepository.countByStatus(BEFORE_OPEN);
		MeetingDemandV2GetMeetingDemandsQueryDto effectiveQueryDto = adjustPageWhenOutOfRange(queryDto, totalCount);
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
	@Transactional
	public MeetingDemandV2CreateMeetingDemandResponseDto createMeetingDemand(
		MeetingDemandV2CreateMeetingDemandBodyDto requestBody, Integer userId) {
		User user = userRepository.findByIdOrThrow(userId);
		List<MeetingKeywordType> meetingKeywordTypes = toMeetingKeywordTypes(requestBody.getMeetingKeywordTypes());

		MeetingDemand meetingDemand = MeetingDemand.builder()
			.user(user)
			.shortIntro(requestBody.getShortIntro())
			.expectation(requestBody.getExpectation())
			.meetingKeywordTypes(meetingKeywordTypes)
			.joinInfo(requestBody.getJoinInfo())
			.build();

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

	private MeetingDemandV2GetMeetingDemandsQueryDto adjustPageWhenOutOfRange(
		MeetingDemandV2GetMeetingDemandsQueryDto queryDto, int totalCount) {
		if (totalCount == 0) {
			return new MeetingDemandV2GetMeetingDemandsQueryDto(1, queryDto.getTake());
		}

		int pageCount = (int)Math.ceil((double)totalCount / queryDto.getTake());
		int normalizedPage = Math.min(queryDto.getPage(), pageCount);

		return new MeetingDemandV2GetMeetingDemandsQueryDto(normalizedPage, queryDto.getTake());
	}

	private List<MeetingKeywordType> toMeetingKeywordTypes(List<String> values) {
		if (values == null || values.isEmpty() || values.size() > MAX_MEETING_KEYWORD_SIZE) {
			throw new BadRequestException(INVALID_MEETING_KEYWORD_SIZE.getErrorCode());
		}

		return values.stream()
			.map(MeetingKeywordType::ofValue)
			.toList();
	}
}
