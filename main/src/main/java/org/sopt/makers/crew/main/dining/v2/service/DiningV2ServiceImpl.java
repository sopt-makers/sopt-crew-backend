package org.sopt.makers.crew.main.dining.v2.service;

import static org.sopt.makers.crew.main.dining.v2.dto.mapper.DiningMeetingMapper.*;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.hibernate.exception.ConstraintViolationException;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningPayerApprovedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningPayerRejectedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningPayerRequestedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningPayerRevokedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningRoundCreatedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.mapper.DiningMeetingMapper;
import org.sopt.makers.crew.main.dining.v2.dto.query.DiningV2GetAttendeesQueryDto;
import org.sopt.makers.crew.main.dining.v2.dto.query.DiningV2GetDiningsQueryDto;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateDiningBodyDto;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateRoundBodyDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2CreateDiningResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2CreateRoundResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetAttendanceResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetAttendeeManagementResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetAttendeesResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetPayerRequestsResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetDiningDetailResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetDiningDetailResponseDto.MyAttendanceStatus;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetDiningsResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetDiningsResponseDto.DiningCard;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2LeaderDto;
import org.sopt.makers.crew.main.entity.dining.DiningAttendance;
import org.sopt.makers.crew.main.entity.dining.DiningAttendanceRepository;
import org.sopt.makers.crew.main.entity.dining.DiningRound;
import org.sopt.makers.crew.main.entity.dining.DiningRoundRepository;
import org.sopt.makers.crew.main.entity.dining.DiningRounds;
import org.sopt.makers.crew.main.entity.dining.enums.DiningPayerStatus;
import org.sopt.makers.crew.main.entity.dining.DiningSearchRepository;
import org.sopt.makers.crew.main.entity.dining.enums.DiningRoundStatus;
import org.sopt.makers.crew.main.entity.dining.projection.DiningAttendeeCountProjection;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.global.config.ImageSettingProperties;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;
import org.sopt.makers.crew.main.global.util.ActiveGenerationProvider;
import org.sopt.makers.crew.main.global.util.Time;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiningV2ServiceImpl implements DiningV2Service {

	private final MeetingRepository meetingRepository;
	private final DiningRoundRepository diningRoundRepository;
	private final DiningAttendanceRepository diningAttendanceRepository;
	private final DiningSearchRepository diningSearchRepository;
	private final UserRepository userRepository;

	private final DiningMeetingMapper diningMeetingMapper;
	private final DiningMapLinkValidator diningMapLinkValidator;
	private final DiningPermissionValidator diningPermissionValidator;
	private final DiningDeletionHistoryRecorder diningDeletionHistoryRecorder;

	private final ImageSettingProperties imageSettingProperties;
	private final ActiveGenerationProvider activeGenerationProvider;
	private final ApplicationEventPublisher eventPublisher;
	private final Time time;

	/**
	 * 회식 등록: 회식(meeting) + 1차 회차 생성, 개설자는 1차에 자동 참석한다.
	 */
	@Override
	@Transactional
	public DiningV2CreateDiningResponseDto createDining(DiningV2CreateAndUpdateDiningBodyDto requestBody,
		Integer userId) {
		userRepository.findByIdOrThrow(userId);

		LocalDate diningDate = parseDiningDate(requestBody.diningDate());
		validateNotPastDiningDate(diningDate);
		diningMapLinkValidator.validate(requestBody.naverMapLink(), requestBody.kakaoMapLink());

		Meeting dining = meetingRepository.save(diningMeetingMapper.toMeetingEntity(
			requestBody.title(),
			getImageUrlsOrDefault(requestBody.files()),
			diningDate,
			normalizeJoinableParts(requestBody.joinableParts()),
			userId,
			time.now(),
			activeGenerationProvider.getActiveGeneration()
		));

		DiningRound firstRound = diningRoundRepository.save(DiningRound.builder()
			.meetingId(dining.getId())
			.place(blankToNull(requestBody.place()))
			.naverMapLink(blankToNull(requestBody.naverMapLink()))
			.kakaoMapLink(blankToNull(requestBody.kakaoMapLink()))
			.startTime(parseStartTime(requestBody.startTime()))
			.build());
		attend(firstRound, userId);

		return DiningV2CreateDiningResponseDto.from(dining.getId());
	}

	/**
	 * 회식 수정: 회식 정보 + 1차 회차 정보. 수정 시에는 지난 날짜도 허용한다.
	 */
	@Override
	@Transactional
	public void updateDining(Integer meetingId, DiningV2CreateAndUpdateDiningBodyDto requestBody, Integer userId) {
		Meeting dining = meetingRepository.findDiningByIdOrThrow(meetingId);
		diningPermissionValidator.validateLeader(dining, userId);
		dining.validateDiningNotEnded();

		LocalDate diningDate = parseDiningDate(requestBody.diningDate());
		diningMapLinkValidator.validate(requestBody.naverMapLink(), requestBody.kakaoMapLink());

		dining.updateDining(
			requestBody.title(),
			getImageUrlsOrDefault(requestBody.files()),
			toStartOfDay(diningDate),
			toEndOfDay(diningDate),
			normalizeJoinableParts(requestBody.joinableParts())
		);

		getRounds(meetingId).getFirstRound().update(
			blankToNull(requestBody.place()),
			blankToNull(requestBody.naverMapLink()),
			blankToNull(requestBody.kakaoMapLink()),
			parseStartTime(requestBody.startTime())
		);
	}

	/**
	 * 회식 삭제(= 1차 삭제): 삭제 이력을 먼저 남기고 참석 → 회차 → 회식 순으로 삭제한다.
	 * 마감된 회차가 있거나 종료된 회식은 삭제할 수 없다.
	 */
	@Override
	@Transactional
	public void deleteDining(Integer meetingId, Integer userId) {
		Meeting dining = meetingRepository.findDiningByIdOrThrow(meetingId);
		diningPermissionValidator.validateLeader(dining, userId);
		dining.validateDiningNotEnded();

		DiningRounds rounds = getRounds(meetingId);
		if (rounds.hasClosedRound()) {
			throw new BadRequestException(CANNOT_DELETE_DINING_WITH_CLOSED_ROUND.getErrorCode());
		}

		List<DiningAttendance> attendances = diningAttendanceRepository.findAllByRoundIdInForUpdate(rounds.getRoundIds());
		diningDeletionHistoryRecorder.recordDiningDeletion(dining, rounds.getRounds(), attendances, userId);

		diningAttendanceRepository.deleteAllInBatch(attendances);
		diningRoundRepository.deleteAllInBatch(rounds.getRounds());
		meetingRepository.delete(dining);
	}

	/**
	 * 회식 종료: 이후 회차 추가와 회식·회차 수정이 불가하며, 마감하지 않은 회차는 함께 마감한다.
	 * 참석자 관리는 종료 후에도 가능하다.
	 */
	@Override
	@Transactional
	public void endDining(Integer meetingId, Integer userId) {
		Meeting dining = meetingRepository.findDiningByIdOrThrow(meetingId);
		diningPermissionValidator.validateLeader(dining, userId);

		LocalDateTime now = time.now();
		dining.endDining(now);
		getRounds(meetingId).getOpenRounds()
			.forEach(round -> round.close(now));
	}

	/**
	 * 회차 추가: 개설자는 새 회차에 자동 참석하고, 커밋 후 이전 회차 참석자에게 알림을 보낸다.
	 */
	@Override
	@Transactional
	public DiningV2CreateRoundResponseDto createRound(Integer meetingId, DiningV2CreateAndUpdateRoundBodyDto requestBody,
		Integer userId) {
		Meeting dining = meetingRepository.findDiningByIdOrThrow(meetingId);
		diningPermissionValidator.validateLeader(dining, userId);
		dining.validateDiningNotEnded();
		diningMapLinkValidator.validate(requestBody.naverMapLink(), requestBody.kakaoMapLink());

		List<Integer> previousRoundIds = getRounds(meetingId).getRoundIds();
		List<Integer> attendeeUserIds = previousRoundIds.isEmpty() ? List.of()
			: diningAttendanceRepository.findDistinctUserIdsByRoundIdIn(previousRoundIds).stream()
				.filter(attendeeUserId -> !attendeeUserId.equals(dining.getUserId()))
				.toList();

		DiningRound round = diningRoundRepository.save(DiningRound.builder()
			.meetingId(meetingId)
			.place(blankToNull(requestBody.place()))
			.naverMapLink(blankToNull(requestBody.naverMapLink()))
			.kakaoMapLink(blankToNull(requestBody.kakaoMapLink()))
			.startTime(parseStartTime(requestBody.startTime()))
			.build());
		attend(round, userId);

		eventPublisher.publishEvent(new DiningRoundCreatedEvent(meetingId, round.getId(), attendeeUserIds));

		return DiningV2CreateRoundResponseDto.from(round.getId());
	}

	/**
	 * 회차 수정: 장소, 링크, 시작 시간만 수정한다. 마감된 회차도 수정할 수 있다.
	 */
	@Override
	@Transactional
	public void updateRound(Integer meetingId, Integer roundId, DiningV2CreateAndUpdateRoundBodyDto requestBody,
		Integer userId) {
		Meeting dining = meetingRepository.findDiningByIdOrThrow(meetingId);
		diningPermissionValidator.validateLeader(dining, userId);
		dining.validateDiningNotEnded();
		diningMapLinkValidator.validate(requestBody.naverMapLink(), requestBody.kakaoMapLink());

		findRoundOfDining(meetingId, roundId).update(
			blankToNull(requestBody.place()),
			blankToNull(requestBody.naverMapLink()),
			blankToNull(requestBody.kakaoMapLink()),
			parseStartTime(requestBody.startTime())
		);
	}

	/**
	 * 회차 삭제(2차 이상): 삭제 이력을 먼저 남기고 참석 → 회차 순으로 삭제한다. 뒤 회차 번호는 자동으로 당겨진다.
	 */
	@Override
	@Transactional
	public void deleteRound(Integer meetingId, Integer roundId, Integer userId) {
		Meeting dining = meetingRepository.findDiningByIdOrThrow(meetingId);
		diningPermissionValidator.validateLeader(dining, userId);
		dining.validateDiningNotEnded();

		DiningRounds rounds = getRounds(meetingId);
		DiningRound round = rounds.getRound(roundId);
		if (rounds.isFirstRound(round)) {
			throw new BadRequestException(CANNOT_DELETE_FIRST_DINING_ROUND.getErrorCode());
		}
		if (round.isClosed()) {
			throw new BadRequestException(CANNOT_DELETE_CLOSED_DINING_ROUND.getErrorCode());
		}

		List<DiningAttendance> attendances = diningAttendanceRepository.findAllByRoundIdInForUpdate(List.of(roundId));
		diningDeletionHistoryRecorder.recordRoundDeletion(round, attendances, userId);

		diningAttendanceRepository.deleteAllInBatch(attendances);
		diningRoundRepository.delete(round);
	}

	/**
	 * 회차 마감(확정)
	 */
	@Override
	@Transactional
	public void closeRound(Integer meetingId, Integer roundId, Integer userId) {
		Meeting dining = meetingRepository.findDiningByIdOrThrow(meetingId);
		diningPermissionValidator.validateLeader(dining, userId);

		findRoundOfDining(meetingId, roundId).close(time.now());
	}

	@Override
	@Transactional
	public void applyAttendance(Integer meetingId, Integer roundId, Integer userId) {
		meetingRepository.findDiningByIdOrThrow(meetingId);
		DiningRound round = findRoundOfDining(meetingId, roundId);
		round.validateNotClosed();
		addAttendance(round, userId);
	}

	@Override
	@Transactional
	public void cancelAttendance(Integer meetingId, Integer roundId, Integer userId) {
		meetingRepository.findDiningByIdOrThrow(meetingId);
		DiningRound round = findRoundOfDining(meetingId, roundId);
		round.validateNotClosed();
		deleteAttendance(round, userId, userId);
	}

	@Override
	public DiningV2GetAttendanceResponseDto getAttendance(Integer meetingId, Integer roundId, Integer userId) {
		meetingRepository.findDiningByIdOrThrow(meetingId);
		findRoundOfDining(meetingId, roundId);
		Optional<DiningAttendance> attendance = diningAttendanceRepository.findByRoundIdAndUserId(roundId, userId);
		return new DiningV2GetAttendanceResponseDto(diningAttendanceRepository.countByRoundId(roundId),
			attendance.isPresent(), attendance.map(DiningAttendance::getPayerStatus).orElse(null));
	}

	@Override
	public DiningV2GetAttendeeManagementResponseDto getAttendees(Integer meetingId, Integer roundId,
		DiningV2GetAttendeesQueryDto queryCommand, Integer userId) {
		Meeting dining = meetingRepository.findDiningByIdOrThrow(meetingId);
		findRoundOfDining(meetingId, roundId);
		diningPermissionValidator.validateLeaderOrApprovedPayer(dining, userId);
		boolean ascending = "asc".equals(queryCommand.getDate());
		Sort.Direction direction = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;
		PageRequest pageable = PageRequest.of(queryCommand.getPage() - 1, queryCommand.getTake(),
			Sort.by(direction, "createdTimestamp", "id"));
		Page<DiningAttendance> page = diningAttendanceRepository.findAllByRoundId(roundId, pageable);
		PageMetaDto meta = new PageMetaDto(queryCommand, Math.toIntExact(page.getTotalElements()));
		if (page.isEmpty()) {
			return new DiningV2GetAttendeeManagementResponseDto(List.of(), meta);
		}
		List<DiningAttendance> attendances = page.getContent();
		Map<Integer, User> users = userRepository.findAllByIdInOrThrow(attendances.stream()
			.map(DiningAttendance::getUserId).toList()).stream()
			.collect(Collectors.toMap(User::getId, user -> user));
		return new DiningV2GetAttendeeManagementResponseDto(IntStream.range(0, attendances.size())
			.mapToObj(index -> DiningV2GetAttendeeManagementResponseDto.Attendee.of(
				users.get(attendances.get(index).getUserId()),
				ascending ? pageable.getOffset() + index + 1 : page.getTotalElements() - pageable.getOffset() - index))
			.toList(), meta);
	}

	@Override
	public DiningV2GetAttendeesResponseDto getAttendanceAttendees(Integer meetingId, Integer roundId, Integer userId) {
		meetingRepository.findDiningByIdOrThrow(meetingId);
		findRoundOfDining(meetingId, roundId);
		List<Integer> userIds = diningAttendanceRepository.findAllByRoundId(roundId).stream()
			.map(DiningAttendance::getUserId).toList();
		if (userIds.isEmpty()) {
			return new DiningV2GetAttendeesResponseDto(List.of());
		}
		return new DiningV2GetAttendeesResponseDto(userRepository.findAllById(userIds).stream()
			.sorted(Comparator.comparing(User::getId))
			.map(DiningV2GetAttendeesResponseDto.Attendee::of).toList());
	}

	@Override
	@Transactional
	public void addAttendee(Integer meetingId, Integer roundId, Integer attendeeUserId, Integer userId) {
		Meeting dining = meetingRepository.findDiningByIdOrThrow(meetingId);
		DiningRound round = findRoundOfDining(meetingId, roundId);
		diningPermissionValidator.validateLeaderOrApprovedPayer(dining, userId);
		addAttendance(round, attendeeUserId);
	}

	@Override
	@Transactional
	public void deleteAttendee(Integer meetingId, Integer roundId, Integer attendeeUserId, Integer userId) {
		Meeting dining = meetingRepository.findDiningByIdOrThrow(meetingId);
		DiningRound round = findRoundOfDining(meetingId, roundId);
		diningPermissionValidator.validateLeaderOrApprovedPayer(dining, userId);
		deleteAttendance(round, attendeeUserId, userId);
	}

	@Override
	@Transactional
	public void requestPayer(Integer meetingId, Integer roundId, Integer userId) {
		Meeting dining = meetingRepository.findDiningByIdOrThrow(meetingId);
		findRoundOfDining(meetingId, roundId);
		DiningAttendance attendance = findAttendanceForPayerUpdate(roundId, userId);
		if (diningPermissionValidator.isLeader(dining, userId)) {
			throw new BadRequestException(CANNOT_REQUEST_DINING_PAYER_AS_LEADER.getErrorCode());
		}
		attendance.requestPayer(time.now());
		User requester = userRepository.findByIdOrThrow(userId);
		eventPublisher.publishEvent(new DiningPayerRequestedEvent(meetingId, roundId, userId,
			dining.getUserId(), dining.getTitle(), requester.getName(), getRounds(meetingId).getRoundNumber(roundId)));
	}

	@Override
	@Transactional
	public void approvePayer(Integer meetingId, Integer roundId, Integer requesterUserId, Integer userId) {
		Meeting dining = meetingRepository.findDiningByIdOrThrow(meetingId);
		diningPermissionValidator.validateLeader(dining, userId);
		findRoundOfDining(meetingId, roundId);
		DiningAttendance attendance = findAttendanceForPayerUpdate(roundId, requesterUserId);
		attendance.approvePayer();
		eventPublisher.publishEvent(new DiningPayerApprovedEvent(meetingId, roundId, requesterUserId, dining.getTitle()));
	}

	@Override
	@Transactional
	public void rejectPayer(Integer meetingId, Integer roundId, Integer requesterUserId, Integer userId) {
		Meeting dining = meetingRepository.findDiningByIdOrThrow(meetingId);
		diningPermissionValidator.validateLeader(dining, userId);
		findRoundOfDining(meetingId, roundId);
		DiningAttendance attendance = findAttendanceForPayerUpdate(roundId, requesterUserId);
		attendance.validatePendingPayer();
		attendance.clearPayer();
		eventPublisher.publishEvent(new DiningPayerRejectedEvent(meetingId, roundId, requesterUserId, dining.getTitle()));
	}

	@Override
	@Transactional
	public void revokePayer(Integer meetingId, Integer roundId, Integer requesterUserId, Integer userId) {
		Meeting dining = meetingRepository.findDiningByIdOrThrow(meetingId);
		diningPermissionValidator.validateLeader(dining, userId);
		findRoundOfDining(meetingId, roundId);
		DiningAttendance attendance = findAttendanceForPayerUpdate(roundId, requesterUserId);
		attendance.validateApprovedPayer();
		attendance.clearPayer();
		eventPublisher.publishEvent(new DiningPayerRevokedEvent(meetingId, roundId, requesterUserId, dining.getTitle(),
			getRounds(meetingId).getRoundNumber(roundId)));
	}

	@Override
	public DiningV2GetPayerRequestsResponseDto getPayerRequests(Integer meetingId, Integer userId) {
		Meeting dining = meetingRepository.findDiningByIdOrThrow(meetingId);
		diningPermissionValidator.validateLeader(dining, userId);
		DiningRounds rounds = getRounds(meetingId);
		List<DiningAttendance> requests = diningAttendanceRepository
			.findAllByRoundIdInAndPayerStatusOrderByPayerRequestedAtAscIdAsc(rounds.getRoundIds(), DiningPayerStatus.PENDING);
		if (requests.isEmpty()) {
			return new DiningV2GetPayerRequestsResponseDto(List.of());
		}
		Map<Integer, User> users = userRepository.findAllByIdInOrThrow(requests.stream()
			.map(DiningAttendance::getUserId).distinct().toList()).stream()
			.collect(Collectors.toMap(User::getId, user -> user));
		return new DiningV2GetPayerRequestsResponseDto(requests.stream()
			.map(attendance -> DiningV2GetPayerRequestsResponseDto.PayerRequest.of(attendance,
				users.get(attendance.getUserId()), rounds.getRoundNumber(attendance.getRoundId())))
			.toList());
	}

	private DiningAttendance findAttendanceForPayerUpdate(Integer roundId, Integer userId) {
		return diningAttendanceRepository.findByRoundIdAndUserIdForUpdate(roundId, userId)
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_DINING_ATTENDANCE.getErrorCode()));
	}

	private void attend(DiningRound round, Integer userId) {
		diningAttendanceRepository.save(DiningAttendance.builder()
			.roundId(round.getId())
			.userId(userId)
			.build());
	}

	private DiningRounds getRounds(Integer meetingId) {
		return new DiningRounds(diningRoundRepository.findAllByMeetingIdOrderByIdAsc(meetingId));
	}

	private DiningRound findRoundOfDining(Integer meetingId, Integer roundId) {
		DiningRound round = diningRoundRepository.findByIdOrThrow(roundId);
		if (!round.getMeetingId().equals(meetingId)) {
			throw new NotFoundException(NOT_FOUND_DINING_ROUND.getErrorCode());
		}
		return round;
	}

	private List<ImageUrlVO> getImageUrlsOrDefault(List<String> files) {
		if (files == null || files.isEmpty()) {
			return toImageUrls(List.of(imageSettingProperties.getDefaultDiningImage()));
		}
		return toImageUrls(files);
	}

	private void validateNotPastDiningDate(LocalDate diningDate) {
		if (diningDate.isBefore(time.now().toLocalDate())) {
			throw new BadRequestException(PAST_DINING_DATE.getErrorCode());
		}
	}

	private void addAttendance(DiningRound round, Integer userId) {
		userRepository.findByIdOrThrow(userId);
		if (diningAttendanceRepository.existsByRoundIdAndUserId(round.getId(), userId)) {
			throw new BadRequestException(ALREADY_ATTENDED_DINING_ROUND.getErrorCode());
		}
		try {
			diningAttendanceRepository.saveAndFlush(DiningAttendance.builder()
				.roundId(round.getId())
				.userId(userId)
				.build());
		} catch (DataIntegrityViolationException exception) {
			// 제약 위반으로 중단된 트랜잭션에서 재조회하지 않고 해당 제약만 판별한다.
			Throwable cause = exception;
			while (cause != null) {
				if (cause instanceof ConstraintViolationException violation
					&& "UQ_dining_attendance_round_user".equals(violation.getConstraintName())) {
					throw new BadRequestException(ALREADY_ATTENDED_DINING_ROUND.getErrorCode());
				}
				cause = cause.getCause();
			}
			throw exception;
		}
	}

	private void deleteAttendance(DiningRound round, Integer attendeeUserId, Integer deletedByUserId) {
		DiningAttendance attendance = diningAttendanceRepository.findByRoundIdAndUserIdForUpdate(round.getId(), attendeeUserId)
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_DINING_ATTENDANCE.getErrorCode()));
		diningDeletionHistoryRecorder.recordAttendanceDeletion(round, attendance, deletedByUserId);
		diningAttendanceRepository.delete(attendance);
	}

	/**
	 * 회식 메인 목록: 전체/내 회식, 진행 상태·파트 필터, 회식명 검색
	 * 정렬: 진행 중 우선 → 회식 일자 최신순 → 등록 최신순
	 */
	@Override
	public DiningV2GetDiningsResponseDto getDinings(DiningV2GetDiningsQueryDto queryCommand, Integer userId) {
		long totalCount = diningSearchRepository.countDinings(queryCommand, userId);
		PageMetaDto meta = new PageMetaDto(queryCommand, Math.toIntExact(totalCount));
		List<Meeting> dinings = totalCount == 0 ? List.of() : diningSearchRepository.findDinings(queryCommand, userId);
		if (dinings.isEmpty()) {
			return new DiningV2GetDiningsResponseDto(List.of(), meta);
		}

		List<Integer> meetingIds = dinings.stream().map(Meeting::getId).toList();
		Map<Integer, Long> attendeeCounts = diningAttendanceRepository.countDistinctUserIdsByMeetingIdIn(meetingIds)
			.stream()
			.collect(Collectors.toMap(DiningAttendeeCountProjection::getMeetingId,
				DiningAttendeeCountProjection::getAttendeeCount));
		Map<Integer, User> leaders = userRepository.findAllById(
				dinings.stream().map(Meeting::getUserId).distinct().toList())
			.stream()
			.collect(Collectors.toMap(User::getId, Function.identity()));
		Set<Integer> approvedPayerMeetingIds = new HashSet<>(
			diningAttendanceRepository.findMeetingIdsByUserIdAndPayerStatus(userId, DiningPayerStatus.APPROVED,
				meetingIds));

		List<DiningCard> cards = dinings.stream()
			.map(dining -> new DiningCard(
				dining.getId(),
				dining.getTitle(),
				dining.getmStartDate().toLocalDate(),
				getFirstImageUrl(dining),
				dining.isDiningEnded(),
				dining.getJoinableParts(),
				attendeeCounts.getOrDefault(dining.getId(), 0L),
				toLeaderDto(leaders.get(dining.getUserId())),
				dining.checkMeetingLeader(userId) || approvedPayerMeetingIds.contains(dining.getId())
			))
			.toList();

		return new DiningV2GetDiningsResponseDto(cards, meta);
	}

	/**
	 * 회식 상세: 회식 정보와 회차 목록. 회차별 인원, 내 참석 상태, 결제자 상태는 참석 기록을 한 번에 읽어 계산한다.
	 */
	@Override
	public DiningV2GetDiningDetailResponseDto getDiningDetail(Integer meetingId, Integer userId) {
		Meeting dining = meetingRepository.findDiningByIdOrThrow(meetingId);
		DiningRounds rounds = getRounds(meetingId);
		List<DiningAttendance> attendances = rounds.getRoundIds().isEmpty()
			? List.of()
			: diningAttendanceRepository.findAllByRoundIdIn(rounds.getRoundIds());

		Map<Integer, Long> attendeeCounts = attendances.stream()
			.collect(Collectors.groupingBy(DiningAttendance::getRoundId, Collectors.counting()));
		Map<Integer, DiningAttendance> myAttendances = attendances.stream()
			.filter(attendance -> attendance.getUserId().equals(userId))
			.collect(Collectors.toMap(DiningAttendance::getRoundId, Function.identity()));
		boolean isApprovedPayer = myAttendances.values().stream().anyMatch(DiningAttendance::isApprovedPayer);

		LocalDateTime now = time.now();
		List<DiningRound> roundList = rounds.getRounds();
		List<DiningV2GetDiningDetailResponseDto.Round> roundDtos = IntStream.range(0, roundList.size())
			.mapToObj(index -> toRoundDto(rounds, index, attendeeCounts, myAttendances, now))
			.toList();

		return new DiningV2GetDiningDetailResponseDto(
			dining.getId(),
			dining.getTitle(),
			dining.getmStartDate().toLocalDate(),
			dining.isDateModified(),
			getFirstImageUrl(dining),
			dining.isDiningEnded(),
			dining.getJoinableParts(),
			toLeaderDto(userRepository.findById(dining.getUserId()).orElse(null)),
			dining.checkMeetingLeader(userId),
			isApprovedPayer,
			roundDtos
		);
	}

	/**
	 * 자동 확정: 회식 일자가 오늘 이전인 회식의 미마감 회차를 모두 마감한다. (스케줄러에서 매일 11:59 호출)
	 * 실행 시점의 회식 일자로 판단하므로 일자를 수정해도 확정 시점이 따라간다.
	 */
	@Override
	@Transactional
	public int closeExpiredRounds() {
		LocalDateTime now = time.now();
		return diningRoundRepository.closeOpenRoundsOfDiningsBefore(DiningRoundStatus.CLOSED, DiningRoundStatus.OPEN,
			MeetingCategory.DINING, now.toLocalDate().atStartOfDay(), now);
	}

	private DiningV2GetDiningDetailResponseDto.Round toRoundDto(DiningRounds rounds, int index,
		Map<Integer, Long> attendeeCounts, Map<Integer, DiningAttendance> myAttendances, LocalDateTime now) {
		List<DiningRound> roundList = rounds.getRounds();
		DiningRound round = roundList.get(index);
		DiningAttendance myAttendance = myAttendances.get(round.getId());
		boolean isAttending = myAttendance != null;

		MyAttendanceStatus myAttendanceStatus = !isAttending ? MyAttendanceStatus.NOT_ATTENDING
			: round.isClosed() ? MyAttendanceStatus.ATTENDED : MyAttendanceStatus.ATTENDING;

		// 신규 회차 안내: 1차가 아닌 최신 회차가 마감 전이고, 이전 회차에는 참석했지만 이 회차는 미신청
		boolean isLatestRound = index == roundList.size() - 1;
		boolean attendedPreviousRound = roundList.subList(0, index).stream()
			.anyMatch(previous -> myAttendances.containsKey(previous.getId()));
		boolean showNewRoundGuide = index > 0 && isLatestRound && !round.isClosed() && !isAttending
			&& attendedPreviousRound;

		return new DiningV2GetDiningDetailResponseDto.Round(
			round.getId(),
			index + 1,
			round.getPlace(),
			round.getNaverMapLink(),
			round.getKakaoMapLink(),
			round.getStartTime(),
			round.isClosed(),
			round.isPlaceModified(),
			round.isLinkModified(),
			round.isStartTimeModified(),
			rounds.isNewRound(round, now),
			attendeeCounts.getOrDefault(round.getId(), 0L),
			myAttendanceStatus,
			isAttending ? myAttendance.getPayerStatus() : null,
			showNewRoundGuide
		);
	}

	private String getFirstImageUrl(Meeting dining) {
		List<ImageUrlVO> imageUrls = dining.getImageURL();
		return imageUrls == null || imageUrls.isEmpty() ? null : imageUrls.get(0).getUrl();
	}

	private DiningV2LeaderDto toLeaderDto(User leader) {
		return leader == null ? null : DiningV2LeaderDto.from(leader);
	}
}
