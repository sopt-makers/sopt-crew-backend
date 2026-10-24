package org.sopt.makers.crew.main.dining.v2.service;

import static org.sopt.makers.crew.main.dining.v2.dto.mapper.DiningMeetingMapper.*;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.hibernate.exception.ConstraintViolationException;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningPayerApprovedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningPayerRejectedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningPayerRequestedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningRoundCreatedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.mapper.DiningMeetingMapper;
import org.sopt.makers.crew.main.dining.v2.dto.query.DiningV2GetAttendeesQueryDto;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateDiningBodyDto;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateRoundBodyDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2CreateDiningResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2CreateRoundResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetAttendanceResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetAttendeeManagementResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetAttendeesResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetPayerRequestsResponseDto;
import org.sopt.makers.crew.main.entity.dining.DiningAttendance;
import org.sopt.makers.crew.main.entity.dining.DiningAttendanceRepository;
import org.sopt.makers.crew.main.entity.dining.DiningRound;
import org.sopt.makers.crew.main.entity.dining.DiningRoundRepository;
import org.sopt.makers.crew.main.entity.dining.DiningRounds;
import org.sopt.makers.crew.main.entity.dining.enums.DiningPayerStatus;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
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

		List<DiningAttendance> attendances = diningAttendanceRepository.findAllByRoundIdIn(rounds.getRoundIds());
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

		List<DiningAttendance> attendances = diningAttendanceRepository.findAllByRoundId(roundId);
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
		dining.validateDiningNotEnded();
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
		dining.validateDiningNotEnded();
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
		dining.validateDiningNotEnded();
		findRoundOfDining(meetingId, roundId);
		DiningAttendance attendance = findAttendanceForPayerUpdate(roundId, requesterUserId);
		attendance.validatePendingPayer();
		attendance.clearPayer();
		eventPublisher.publishEvent(new DiningPayerRejectedEvent(meetingId, roundId, requesterUserId, dining.getTitle()));
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
		DiningAttendance attendance = diningAttendanceRepository.findByRoundIdAndUserId(round.getId(), attendeeUserId)
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_DINING_ATTENDANCE.getErrorCode()));
		diningDeletionHistoryRecorder.recordAttendanceDeletion(round, attendance, deletedByUserId);
		diningAttendanceRepository.delete(attendance);
	}
}
