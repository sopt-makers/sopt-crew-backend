package org.sopt.makers.crew.main.meetingdemand.v2.service;

import static org.sopt.makers.crew.main.external.notification.PushNotificationEnums.PUSH_NOTIFICATION_CATEGORY;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemand;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandRepository;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandWaitRepository;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandCommentRepository;
import org.sopt.makers.crew.main.external.notification.PushNotificationService;
import org.sopt.makers.crew.main.external.notification.dto.request.PushNotificationRequestDto;
import org.sopt.makers.crew.main.global.config.PushNotificationProperties;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MeetingDemandNotificationSender {

	private static final String MEETING_DEMAND_OPENED_TITLE = "기다리던 모임이 열렸어요";
	private static final String MEETING_DEMAND_OPENED_CONTENT = "관심을 보였던 수요가 모임으로 개설됐어요.";
	private static final String MEETING_DEMAND_WAIT_TITLE = "내가 만든 모임 수요를 기다리는 사람이 생겼어요";
	private static final String MEETING_DEMAND_WAIT_CONTENT = "수요에 관심을 보인 멤버가 있어요.";
	private static final String MEETING_DETAIL_WEB_LINK_FORMAT = "%s/detail?id=%d";
	private static final String MEETING_DEMAND_WEB_LINK_FORMAT = "%s/meeting-demand?id=%d";

	private final MeetingDemandRepository meetingDemandRepository;
	private final MeetingDemandCommentRepository meetingDemandCommentRepository;
	private final MeetingDemandWaitRepository meetingDemandWaitRepository;
	private final PushNotificationService pushNotificationService;
	private final PushNotificationProperties pushNotificationProperties;

	public void sendOpenedMeetingNotification(Meeting meeting) {
		MeetingDemand meetingDemand = meetingDemandRepository.findByIdOrThrow(meeting.getMeetingDemandId());
		String[] receiverUserIds = getOpenedMeetingReceiverUserIds(meetingDemand, meeting.getUserId());
		if (receiverUserIds.length == 0) {
			return;
		}

		pushNotificationService.sendPushNotification(PushNotificationRequestDto.of(
			receiverUserIds,
			MEETING_DEMAND_OPENED_TITLE,
			MEETING_DEMAND_OPENED_CONTENT,
			PUSH_NOTIFICATION_CATEGORY.getValue(),
			createMeetingDetailWebLink(meeting.getId())
		));
	}

	public void sendWaitNotification(MeetingDemand meetingDemand) {
		pushNotificationService.sendPushNotification(PushNotificationRequestDto.of(
			new String[] {String.valueOf(meetingDemand.getUserId())},
			MEETING_DEMAND_WAIT_TITLE,
			MEETING_DEMAND_WAIT_CONTENT,
			PUSH_NOTIFICATION_CATEGORY.getValue(),
			createMeetingDemandWebLink(meetingDemand.getId())
		));
	}

	private String[] getOpenedMeetingReceiverUserIds(MeetingDemand meetingDemand, Integer meetingLeaderUserId) {
		Set<Integer> receiverUserIds = new LinkedHashSet<>();
		receiverUserIds.add(meetingDemand.getUserId());
		receiverUserIds.addAll(getCommentWriterUserIds(meetingDemand.getId()));
		receiverUserIds.addAll(meetingDemandWaitRepository.findUserIdsByMeetingDemandId(meetingDemand.getId()));
		receiverUserIds.removeIf(Objects::isNull);
		receiverUserIds.remove(meetingLeaderUserId);

		return receiverUserIds.stream()
			.map(String::valueOf)
			.toArray(String[]::new);
	}

	private List<Integer> getCommentWriterUserIds(Integer meetingDemandId) {
		return meetingDemandCommentRepository.findDistinctUserIdsByMeetingDemandId(meetingDemandId);
	}

	private String createMeetingDetailWebLink(Integer meetingId) {
		return String.format(MEETING_DETAIL_WEB_LINK_FORMAT, pushNotificationProperties.getPushWebUrl(), meetingId);
	}

	private String createMeetingDemandWebLink(Integer meetingDemandId) {
		return String.format(MEETING_DEMAND_WEB_LINK_FORMAT, pushNotificationProperties.getPushWebUrl(),
			meetingDemandId);
	}
}
