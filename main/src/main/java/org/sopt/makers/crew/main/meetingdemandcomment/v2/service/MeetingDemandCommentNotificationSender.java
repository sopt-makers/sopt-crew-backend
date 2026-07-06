package org.sopt.makers.crew.main.meetingdemandcomment.v2.service;

import static org.sopt.makers.crew.main.external.notification.PushNotificationEnums.PUSH_NOTIFICATION_CATEGORY;

import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemand;
import org.sopt.makers.crew.main.external.notification.PushNotificationService;
import org.sopt.makers.crew.main.external.notification.dto.request.PushNotificationRequestDto;
import org.sopt.makers.crew.main.global.config.PushNotificationProperties;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.request.MeetingDemandCommentV2MentionUserInCommentRequestDto;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MeetingDemandCommentNotificationSender {

	private static final String MEETING_DEMAND_COMMENT_TITLE = "내가 만든 모임 수요에 댓글이 달렸어요";
	private static final String MEETING_DEMAND_COMMENT_CONTENT = "새로운 댓글이 달렸어요.";
	private static final String MEETING_DEMAND_WEB_LINK_FORMAT = "%s/meeting-demand?id=%d";

	private final PushNotificationService pushNotificationService;
	private final PushNotificationProperties pushNotificationProperties;

	public void sendCommentNotification(MeetingDemand meetingDemand, Integer writerUserId) {
		if (writerUserId == null || meetingDemand.isWriter(writerUserId)) {
			return;
		}

		String webLink = createMeetingDemandWebLink(meetingDemand.getId());

		PushNotificationRequestDto pushRequestDto = PushNotificationRequestDto.of(
			new String[] {String.valueOf(meetingDemand.getUserId())},
			MEETING_DEMAND_COMMENT_TITLE,
			MEETING_DEMAND_COMMENT_CONTENT,
			PUSH_NOTIFICATION_CATEGORY.getValue(),
			webLink
		);

		pushNotificationService.sendPushNotification(pushRequestDto);
	}

	public void sendMentionNotification(MeetingDemandCommentV2MentionUserInCommentRequestDto requestBody) {
		String webLink = createMeetingDemandWebLink(requestBody.getMeetingDemandId());

		String[] userOrgIds = requestBody.getOrgIds().stream()
			.map(Object::toString)
			.toArray(String[]::new);

		PushNotificationRequestDto pushRequestDto = PushNotificationRequestDto.of(
			userOrgIds,
			MEETING_DEMAND_COMMENT_TITLE,
			MEETING_DEMAND_COMMENT_CONTENT,
			PUSH_NOTIFICATION_CATEGORY.getValue(),
			webLink
		);

		pushNotificationService.sendPushNotification(pushRequestDto);
	}

	private String createMeetingDemandWebLink(Integer meetingDemandId) {
		return String.format(MEETING_DEMAND_WEB_LINK_FORMAT, pushNotificationProperties.getPushWebUrl(),
			meetingDemandId);
	}
}
