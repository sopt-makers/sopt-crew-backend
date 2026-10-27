package org.sopt.makers.crew.main.dining.v2.service;

import static org.sopt.makers.crew.main.external.notification.PushNotificationEnums.*;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.sopt.makers.crew.main.dining.v2.dto.event.DiningAttendeeAddedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningAttendeeRemovedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningCreatedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningDeletedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningPayerApprovedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningPayerRejectedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningPayerRequestedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningPayerRevokedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningRoundCreatedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningRoundDeletedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningRoundUpdatedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.event.DiningUpdatedEvent;
import org.sopt.makers.crew.main.dining.v2.dto.notification.DiningCloseGuideTarget;
import org.sopt.makers.crew.main.dining.v2.dto.notification.DiningReminderTarget;
import org.sopt.makers.crew.main.entity.dining.DiningRoundRepository;
import org.sopt.makers.crew.main.entity.dining.DiningRounds;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.external.notification.PushNotificationService;
import org.sopt.makers.crew.main.external.notification.dto.request.PushNotificationRequestDto;
import org.sopt.makers.crew.main.global.config.PushNotificationProperties;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 회식 푸시 알림. 수신자와 표시 값은 상태 변경 트랜잭션에서 확정해 이벤트로 전달받는다.
 * (회식 등록 알림만 대상이 많아 커밋 후 현재 기수 회원을 조회한다. 마감 안내·리마인드는 스케줄러가 대상을 조회한다.)
 */
@Component
@RequiredArgsConstructor
public class DiningNotificationSender {

	private static final String DINING_CREATED_TITLE = "새로운 회식 등록";
	private static final String DINING_CREATED_CONTENT = "[%s] 함께할 분은 지금 바로 신청해주세요.";
	private static final String NEW_ROUND_TITLE = "새로운 회차 추가";
	private static final String NEW_ROUND_CONTENT = "[%s] %d차에 참석하려면 신청해주세요.";
	private static final String ROUND_UPDATED_TITLE = "회차 정보 수정";
	private static final String ROUND_UPDATED_CONTENT = "[%s] %d차 정보가 수정됐어요. 변경된 내용을 확인해주세요.";
	private static final String DINING_UPDATED_TITLE = "회식 정보 수정";
	private static final String DINING_UPDATED_CONTENT = "[%s] 변경된 내용을 확인해주세요.";
	private static final String ROUND_DELETED_TITLE = "회차 삭제";
	private static final String ROUND_DELETED_CONTENT = "[%s] %d차 회식이 삭제됐어요.";
	private static final String DINING_DELETED_TITLE = "회식 삭제";
	private static final String DINING_DELETED_CONTENT = "[%s] 개설자가 회식을 삭제했어요.";
	private static final String ATTENDEE_ADDED_TITLE = "회식 참석자 추가";
	private static final String ATTENDEE_ADDED_CONTENT = "[%s] %d차 참석자로 추가됐어요.";
	private static final String ATTENDEE_REMOVED_TITLE = "회식 참석자 제외";
	private static final String ATTENDEE_REMOVED_CONTENT = "[%s] %d차 참석자에서 제외됐어요.";
	private static final String CLOSE_GUIDE_TITLE = "마감하지 않은 회차";
	private static final String CLOSE_GUIDE_CONTENT = "[%s] 내일 오전 11:59에 자동으로 마감돼요.";
	private static final String REMINDER_TITLE = "내일 예정된 회식";
	private static final String REMINDER_CONTENT = "[%s] 회식 정보를 확인해주세요.";
	private static final String PAYER_REQUESTED_TITLE = "공동 결제자 승인 요청";
	private static final String PAYER_REQUESTED_CONTENT = "[%s] %d차 [%s]님이 승인을 요청했어요. 참석자 관리 화면에서 확인해주세요.";
	private static final String PAYER_APPROVED_TITLE = "공동 결제자 요청 승인";
	private static final String PAYER_APPROVED_CONTENT = "[%s] %d차 요청이 승인됐어요. 이제 참석자 관리 화면을 이용할 수 있어요.";
	private static final String PAYER_REJECTED_TITLE = "공동 결제자 요청 거절";
	private static final String PAYER_REJECTED_CONTENT = "[%s] %d차 공동 결제자 요청이 거절됐어요.";
	private static final String PAYER_REVOKED_TITLE = "공동 결제자 권한 해제";
	private static final String PAYER_REVOKED_CONTENT = "[%s] %d차 공동 결제자 권한이 해제됐어요.";

	// TODO: FE 회식 탭 경로 확정 후 변경 예정 (현재 임의 경로)
	private static final String DINING_TAB_WEB_LINK_FORMAT = "%s/dining";
	// TODO: FE 회식 상세 페이지 경로 확정 후 변경 예정 (현재 임의 경로)
	private static final String DINING_DETAIL_WEB_LINK_FORMAT = "%s/dining/detail?id=%d";
	// TODO: FE 참석자 관리 및 결제자 요청 영역 경로 확정 후 변경
	private static final String DINING_ATTENDEE_WEB_LINK_FORMAT = "%s/dining/attendees?id=%d";
	private static final String PAYER_REQUESTS_ANCHOR = "#payer-requests";

	private final MeetingRepository meetingRepository;
	private final DiningRoundRepository diningRoundRepository;
	private final UserRepository userRepository;
	private final PushNotificationService pushNotificationService;
	private final PushNotificationProperties pushNotificationProperties;

	/**
	 * 현재 활동 기수에서 참여 파트에 속한 회원에게 알린다. (개설자 제외, 회식이 삭제됐으면 보내지 않음)
	 */
	public void sendDiningCreatedNotification(DiningCreatedEvent event) {
		if (findDining(event.meetingId()).isEmpty()) {
			return;
		}
		List<Integer> receiverUserIds = userRepository.findAllByActivityGeneration(event.activeGeneration()).stream()
			.filter(user -> !user.getId().equals(event.leaderUserId()))
			.filter(user -> isInJoinableParts(user, event))
			.map(User::getId)
			.toList();
		send(receiverUserIds, DINING_CREATED_TITLE,
			String.format(DINING_CREATED_CONTENT, event.diningTitle()), detailLink(event.meetingId()));
	}

	/**
	 * 새 회차 생성 트랜잭션에서 확정한 현재 참석자에게 알린다.
	 * 비동기 발송 시점의 신청·취소로 수신자를 바꾸지 않으며, 회식이나 대상 회차가 삭제됐으면 보내지 않는다.
	 */
	public void sendNewRoundNotification(DiningRoundCreatedEvent event) {
		Integer meetingId = event.meetingId();
		Integer roundId = event.roundId();
		Optional<Meeting> dining = findDining(meetingId);
		if (dining.isEmpty()) {
			return;
		}

		DiningRounds rounds = new DiningRounds(diningRoundRepository.findAllByMeetingIdOrderByIdAsc(meetingId));
		if (!rounds.getRoundIds().contains(roundId)) {
			return;
		}

		Integer leaderUserId = dining.get().getUserId();
		send(event.attendeeUserIds().stream().filter(userId -> !userId.equals(leaderUserId)).toList(),
			NEW_ROUND_TITLE, String.format(NEW_ROUND_CONTENT, dining.get().getTitle(), rounds.getRoundNumber(roundId)),
			detailLink(meetingId));
	}

	public void sendRoundUpdatedNotification(DiningRoundUpdatedEvent event) {
		send(event.receiverUserIds(), ROUND_UPDATED_TITLE,
			String.format(ROUND_UPDATED_CONTENT, event.diningTitle(), event.roundNumber()), detailLink(event.meetingId()));
	}

	public void sendDiningUpdatedNotification(DiningUpdatedEvent event) {
		send(event.receiverUserIds(), DINING_UPDATED_TITLE,
			String.format(DINING_UPDATED_CONTENT, event.diningTitle()), tabLink());
	}

	public void sendRoundDeletedNotification(DiningRoundDeletedEvent event) {
		send(event.receiverUserIds(), ROUND_DELETED_TITLE,
			String.format(ROUND_DELETED_CONTENT, event.diningTitle(), event.roundNumber()), detailLink(event.meetingId()));
	}

	public void sendDiningDeletedNotification(DiningDeletedEvent event) {
		send(event.receiverUserIds(), DINING_DELETED_TITLE,
			String.format(DINING_DELETED_CONTENT, event.diningTitle()), tabLink());
	}

	public void sendAttendeeAddedNotification(DiningAttendeeAddedEvent event) {
		send(List.of(event.attendeeUserId()), ATTENDEE_ADDED_TITLE,
			String.format(ATTENDEE_ADDED_CONTENT, event.diningTitle(), event.roundNumber()), detailLink(event.meetingId()));
	}

	public void sendAttendeeRemovedNotification(DiningAttendeeRemovedEvent event) {
		send(List.of(event.attendeeUserId()), ATTENDEE_REMOVED_TITLE,
			String.format(ATTENDEE_REMOVED_CONTENT, event.diningTitle(), event.roundNumber()),
			detailLink(event.meetingId()));
	}

	/** @return 발송 실패 시 false (스케줄러 실패 집계용) */
	public boolean sendCloseGuideNotification(DiningCloseGuideTarget target) {
		return send(List.of(target.leaderUserId()), CLOSE_GUIDE_TITLE,
			String.format(CLOSE_GUIDE_CONTENT, target.diningTitle()), attendeeLink(target.meetingId()));
	}

	/** @return 발송 실패 시 false (스케줄러 실패 집계용) */
	public boolean sendReminderNotification(DiningReminderTarget target) {
		return send(target.receiverUserIds(), REMINDER_TITLE,
			String.format(REMINDER_CONTENT, target.diningTitle()), detailLink(target.meetingId()));
	}

	public void sendPayerRequestedNotification(DiningPayerRequestedEvent event) {
		send(List.of(event.leaderUserId()), PAYER_REQUESTED_TITLE,
			String.format(PAYER_REQUESTED_CONTENT, event.diningTitle(), event.roundNumber(), event.requesterName()),
			attendeeLink(event.meetingId()) + PAYER_REQUESTS_ANCHOR);
	}

	public void sendPayerApprovedNotification(DiningPayerApprovedEvent event) {
		send(List.of(event.requesterUserId()), PAYER_APPROVED_TITLE,
			String.format(PAYER_APPROVED_CONTENT, event.diningTitle(), event.roundNumber()),
			attendeeLink(event.meetingId()));
	}

	public void sendPayerRejectedNotification(DiningPayerRejectedEvent event) {
		send(List.of(event.requesterUserId()), PAYER_REJECTED_TITLE,
			String.format(PAYER_REJECTED_CONTENT, event.diningTitle(), event.roundNumber()),
			detailLink(event.meetingId()));
	}

	public void sendPayerRevokedNotification(DiningPayerRevokedEvent event) {
		send(List.of(event.requesterUserId()), PAYER_REVOKED_TITLE,
			String.format(PAYER_REVOKED_CONTENT, event.diningTitle(), event.roundNumber()),
			detailLink(event.meetingId()));
	}

	private Optional<Meeting> findDining(Integer meetingId) {
		return meetingRepository.findById(meetingId)
			.filter(meeting -> meeting.getCategory() == MeetingCategory.DINING);
	}

	private boolean isInJoinableParts(User user, DiningCreatedEvent event) {
		return user.getActivities() != null && user.getActivities().stream()
			.filter(activity -> Objects.equals(activity.getGeneration(), event.activeGeneration()))
			.map(activity -> DiningUserPartResolver.resolve(activity.getPart()))
			.flatMap(Optional::stream)
			.anyMatch(event.joinableParts()::contains);
	}

	/**
	 * 중복 수신자는 1건으로 합치고, 수신자가 없으면 보내지 않는다.
	 * @return push 서버 발송 요청 성공 여부. 수신자가 없어 보내지 않은 경우는 실패가 아니므로 true
	 */
	private boolean send(Collection<Integer> receiverUserIds, String title, String content, String webLink) {
		String[] receivers = receiverUserIds.stream()
			.filter(Objects::nonNull)
			.distinct()
			.map(String::valueOf)
			.toArray(String[]::new);
		if (receivers.length == 0) {
			return true;
		}
		return pushNotificationService.sendPushNotification(PushNotificationRequestDto.of(
			receivers, title, content, PUSH_NOTIFICATION_CATEGORY.getValue(), webLink));
	}

	private String detailLink(Integer meetingId) {
		return String.format(DINING_DETAIL_WEB_LINK_FORMAT, pushNotificationProperties.getPushWebUrl(), meetingId);
	}

	private String attendeeLink(Integer meetingId) {
		return String.format(DINING_ATTENDEE_WEB_LINK_FORMAT, pushNotificationProperties.getPushWebUrl(), meetingId);
	}

	private String tabLink() {
		return String.format(DINING_TAB_WEB_LINK_FORMAT, pushNotificationProperties.getPushWebUrl());
	}
}
