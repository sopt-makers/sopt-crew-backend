package org.sopt.makers.crew.main.dining.v2.service;

import static org.sopt.makers.crew.main.external.notification.PushNotificationEnums.*;

import java.util.Optional;

import org.sopt.makers.crew.main.dining.v2.dto.event.DiningPayerApprovedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningPayerRequestedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningRoundCreatedEvent;
import org.sopt.makers.crew.main.entity.dining.DiningRoundRepository;
import org.sopt.makers.crew.main.entity.dining.DiningRounds;
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

	// TODO: FE 참석자 관리 및 결제자 요청 영역 경로 확정 후 변경
	private static final String DINING_ATTENDEE_WEB_LINK_FORMAT = "%s/dining/attendees?id=%d";
	private static final String PAYER_REQUESTS_ANCHOR = "#payer-requests";

	private final MeetingRepository meetingRepository;
	private final DiningRoundRepository diningRoundRepository;
	private final PushNotificationService pushNotificationService;
	private final PushNotificationProperties pushNotificationProperties;

	/**
	 * 새 회차 생성 트랜잭션에서 확정한 현재 참석자에게 알린다.
	 * 비동기 발송 시점의 신청·취소로 수신자를 바꾸지 않으며, 회식이나 대상 회차가 삭제됐으면 보내지 않는다.
	 */
	public void sendNewRoundNotification(DiningRoundCreatedEvent event) {
		Integer meetingId = event.meetingId();
		Integer roundId = event.roundId();
		Optional<Meeting> dining = meetingRepository.findById(meetingId)
			.filter(meeting -> meeting.getCategory() == MeetingCategory.DINING);
		if (dining.isEmpty()) {
			return;
		}

		DiningRounds rounds = new DiningRounds(diningRoundRepository.findAllByMeetingIdOrderByIdAsc(meetingId));
		if (!rounds.getRoundIds().contains(roundId)) {
			return;
		}

		Integer leaderUserId = dining.get().getUserId();
		String[] receiverUserIds = event.attendeeUserIds().stream()
			.distinct()
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

	public void sendPayerRequestedNotification(DiningPayerRequestedEvent event) {
		sendPayerNotification(event.leaderUserId(), "공동 결제자 승인 요청",
			String.format("[%s] %d차 [%s]님이 공동 결제자 승인을 요청했어요.\n참석자 관리 화면에서 확인해주세요.",
				event.diningTitle(), event.roundNumber(), event.requesterName()),
			String.format(DINING_ATTENDEE_WEB_LINK_FORMAT, pushNotificationProperties.getPushWebUrl(), event.meetingId())
				+ PAYER_REQUESTS_ANCHOR);
	}

	public void sendPayerApprovedNotification(DiningPayerApprovedEvent event) {
		sendPayerNotification(event.requesterUserId(), "공동 결제자 승인",
			String.format("[%s] 공동 결제자로 승인됐어요.\n이제 참석자 관리 화면을 이용할 수 있어요.", event.diningTitle()),
			String.format(DINING_ATTENDEE_WEB_LINK_FORMAT, pushNotificationProperties.getPushWebUrl(), event.meetingId()));
	}

	private void sendPayerNotification(Integer receiverUserId, String title, String content, String webLink) {
		pushNotificationService.sendPushNotification(PushNotificationRequestDto.of(
			new String[] {String.valueOf(receiverUserId)}, title, content,
			PUSH_NOTIFICATION_CATEGORY.getValue(), webLink));
	}
}
