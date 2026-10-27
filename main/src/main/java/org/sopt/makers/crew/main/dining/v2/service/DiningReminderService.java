package org.sopt.makers.crew.main.dining.v2.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.sopt.makers.crew.main.dining.v2.dto.notification.DiningCloseGuideTarget;
import org.sopt.makers.crew.main.dining.v2.dto.notification.DiningReminderTarget;
import org.sopt.makers.crew.main.entity.dining.DiningAttendance;
import org.sopt.makers.crew.main.entity.dining.DiningAttendanceRepository;
import org.sopt.makers.crew.main.entity.dining.DiningRound;
import org.sopt.makers.crew.main.entity.dining.DiningRoundRepository;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.global.util.Time;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 회식 알림 스케줄러의 발송 대상 조회. 발송은 조회 트랜잭션 밖에서 한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiningReminderService {

	private final MeetingRepository meetingRepository;
	private final DiningRoundRepository diningRoundRepository;
	private final DiningAttendanceRepository diningAttendanceRepository;
	private final Time time;

	/**
	 * 오늘이 회식 일자이고, 종료되지 않았으며, 마감하지 않은 회차가 있는 회식의 개설자.
	 */
	public List<DiningCloseGuideTarget> findCloseGuideTargets() {
		List<Meeting> dinings = findOngoingDiningsOn(time.now().toLocalDate());
		if (dinings.isEmpty()) {
			return List.of();
		}
		Set<Integer> diningIdsWithOpenRound = diningRoundRepository
			.findAllByMeetingIdInOrderByIdAsc(dinings.stream().map(Meeting::getId).toList()).stream()
			.filter(round -> !round.isClosed())
			.map(DiningRound::getMeetingId)
			.collect(Collectors.toSet());
		return dinings.stream()
			.filter(dining -> diningIdsWithOpenRound.contains(dining.getId()))
			.map(dining -> new DiningCloseGuideTarget(dining.getId(), dining.getTitle(), dining.getUserId()))
			.toList();
	}

	/**
	 * 내일이 회식 일자이고 종료되지 않은 회식의 전체 회차 참석자. 실행 시점(전날 16:00)에 있는 회식만 대상이다.
	 */
	public List<DiningReminderTarget> findReminderTargets() {
		List<Meeting> dinings = findOngoingDiningsOn(time.now().toLocalDate().plusDays(1));
		if (dinings.isEmpty()) {
			return List.of();
		}
		Map<Integer, Integer> meetingIdByRoundId = diningRoundRepository
			.findAllByMeetingIdInOrderByIdAsc(dinings.stream().map(Meeting::getId).toList()).stream()
			.collect(Collectors.toMap(DiningRound::getId, DiningRound::getMeetingId));
		Map<Integer, List<Integer>> attendeesByMeetingId = meetingIdByRoundId.isEmpty() ? Map.of()
			: diningAttendanceRepository.findAllByRoundIdIn(List.copyOf(meetingIdByRoundId.keySet())).stream()
				.collect(Collectors.groupingBy(attendance -> meetingIdByRoundId.get(attendance.getRoundId()),
					Collectors.mapping(DiningAttendance::getUserId,
						Collectors.collectingAndThen(Collectors.toList(), ids -> ids.stream().distinct().toList()))));
		return dinings.stream()
			.filter(dining -> attendeesByMeetingId.containsKey(dining.getId()))
			.map(dining -> new DiningReminderTarget(dining.getId(), dining.getTitle(),
				attendeesByMeetingId.get(dining.getId())))
			.toList();
	}

	private List<Meeting> findOngoingDiningsOn(LocalDate date) {
		return meetingRepository.findOngoingDiningsByDateBetween(MeetingCategory.DINING,
			date.atStartOfDay(), date.plusDays(1).atStartOfDay());
	}
}
