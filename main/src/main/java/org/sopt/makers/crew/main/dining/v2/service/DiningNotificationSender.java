package org.sopt.makers.crew.main.dining.v2.service;

import static org.sopt.makers.crew.main.external.notification.PushNotificationEnums.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.sopt.makers.crew.main.entity.dining.DiningAttendanceRepository;
import org.sopt.makers.crew.main.entity.dining.DiningDeletionHistory;
import org.sopt.makers.crew.main.entity.dining.DiningDeletionHistoryRepository;
import org.sopt.makers.crew.main.entity.dining.DiningRound;
import org.sopt.makers.crew.main.entity.dining.DiningRoundRepository;
import org.sopt.makers.crew.main.entity.dining.DiningRounds;
import org.sopt.makers.crew.main.entity.dining.enums.DiningDeletionTargetType;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.external.notification.PushNotificationService;
import org.sopt.makers.crew.main.external.notification.dto.request.PushNotificationRequestDto;
import org.sopt.makers.crew.main.global.config.PushNotificationProperties;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 회식 푸시 알림 발송
 * 회식은 2차, 3차가 밤늦게 추가되는 경우가 많아 번쩍과 달리 야간 발송 제한(NotificationTimeValidator)을 두지 않는다.
 */
@Component
@RequiredArgsConstructor
public class DiningNotificationSender {

	// TODO: 기획 확정 문구로 변경 예정 (현재 임의 문구)
	private static final String NEW_ROUND_TITLE = "🍻 회식에 새로운 회차가 추가됐어요";
	// TODO: 기획 확정 문구로 변경 예정 (현재 임의 문구)
	private static final String NEW_ROUND_CONTENT_FORMAT = "[%s] 새로 추가된 %d차에 참석하려면 신청해주세요.";
	// TODO: FE 회식 상세 페이지 경로 확정 후 변경 예정 (현재 임의 경로)
	private static final String DINING_DETAIL_WEB_LINK_FORMAT = "%s/dining/detail?id=%d";

	private final MeetingRepository meetingRepository;
	private final DiningRoundRepository diningRoundRepository;
	private final DiningAttendanceRepository diningAttendanceRepository;
	private final DiningDeletionHistoryRepository diningDeletionHistoryRepository;
	private final PushNotificationService pushNotificationService;
	private final PushNotificationProperties pushNotificationProperties;

	/**
	 * 새 회차 알림. 대상: 새 회차 이전 회차에 한 번이라도 참석한 사람 (취소·삭제 이력 포함, 개설자 제외)
	 * 커밋 이후 비동기로 실행되므로, 그 사이 회식이나 회차가 삭제됐으면 보내지 않고
	 * 그 사이 더 뒤 회차가 추가됐어도 새 회차 이전 회차의 참석자만 대상으로 한다.
	 */
	public void sendNewRoundNotification(Integer meetingId, Integer roundId) {
		Optional<Meeting> dining = meetingRepository.findById(meetingId)
			.filter(meeting -> meeting.getCategory() == MeetingCategory.DINING);
		if (dining.isEmpty()) {
			return;
		}

		DiningRounds rounds = new DiningRounds(diningRoundRepository.findAllByMeetingIdOrderByIdAsc(meetingId));
		if (!rounds.getRoundIds().contains(roundId)) {
			return;
		}

		List<Integer> previousRoundIds = rounds.getRounds().stream()
			.map(DiningRound::getId)
			.filter(id -> id < roundId)
			.toList();
		Set<Integer> attendeeUserIds = new LinkedHashSet<>();
		if (!previousRoundIds.isEmpty()) {
			attendeeUserIds.addAll(diningAttendanceRepository.findDistinctUserIdsByRoundIdIn(previousRoundIds));
		}
		for (DiningDeletionHistory history : diningDeletionHistoryRepository.findAllByMeetingIdAndTargetTypeIn(
			meetingId, List.of(DiningDeletionTargetType.ATTENDANCE, DiningDeletionTargetType.ROUND))) {
			Map<?, ?> data = (Map<?, ?>)history.getSnapshot().get("data");
			if (((Number)data.get("roundId")).intValue() >= roundId) {
				continue;
			}
			if (history.getTargetType() == DiningDeletionTargetType.ATTENDANCE) {
				attendeeUserIds.add(((Number)data.get("userId")).intValue());
			} else {
				for (Object attendance : (List<?>)data.get("attendances")) {
					attendeeUserIds.add(((Number)((Map<?, ?>)attendance).get("userId")).intValue());
				}
			}
		}

		Integer leaderUserId = dining.get().getUserId();
		String[] receiverUserIds = attendeeUserIds.stream()
			.filter(userId -> !userId.equals(leaderUserId))
			.map(String::valueOf)
			.toArray(String[]::new);
		if (receiverUserIds.length == 0) {
			return;
		}

		pushNotificationService.sendPushNotification(PushNotificationRequestDto.of(
			receiverUserIds,
			NEW_ROUND_TITLE,
			String.format(NEW_ROUND_CONTENT_FORMAT, dining.get().getTitle(), rounds.getRoundNumber(roundId)),
			PUSH_NOTIFICATION_CATEGORY.getValue(),
			String.format(DINING_DETAIL_WEB_LINK_FORMAT, pushNotificationProperties.getPushWebUrl(), meetingId)
		));
	}
}
