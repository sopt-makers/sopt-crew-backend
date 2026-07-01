package org.sopt.makers.crew.main.meetingdemandcomment.v2.service;

import static org.sopt.makers.crew.main.external.notification.PushNotificationEnums.PUSH_NOTIFICATION_CATEGORY;

import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemand;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandComment;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandCommentProfile;
import org.sopt.makers.crew.main.external.notification.PushNotificationService;
import org.sopt.makers.crew.main.external.notification.dto.request.PushNotificationRequestDto;
import org.sopt.makers.crew.main.global.config.PushNotificationProperties;
import org.sopt.makers.crew.main.global.util.MentionSecretStringRemover;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.request.MeetingDemandCommentV2CreateCommentBodyDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.request.MeetingDemandCommentV2MentionUserInCommentRequestDto;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MeetingDemandCommentNotificationSender {

	private static final String MEETING_DEMAND_COMMENT_TITLE = "나의 모임 수요에 새로운 댓글이 달렸어요";
	private static final String MEETING_DEMAND_REPLY_TITLE = "나의 모임 수요 댓글에 새로운 답글이 달렸어요";
	private static final String MEETING_DEMAND_MENTION_TITLE_FORMAT = "%s님이 회원님을 언급했어요";
	private static final String MEETING_DEMAND_WEB_LINK_FORMAT = "%s/meeting-demand?id=%d";

	private final PushNotificationService pushNotificationService;
	private final PushNotificationProperties pushNotificationProperties;

	public void sendCommentNotification(MeetingDemandCommentV2CreateCommentBodyDto requestBody,
		MeetingDemand meetingDemand, MeetingDemandComment parentComment, MeetingDemandCommentProfile writerProfile) {
		boolean isReplyComment = !requestBody.getIsParent();
		Integer receiverUserId = isReplyComment ? getReplyReceiverUserId(parentComment) : meetingDemand.getUserId();
		if (receiverUserId == null) {
			return;
		}

		String title = isReplyComment ? MEETING_DEMAND_REPLY_TITLE : MEETING_DEMAND_COMMENT_TITLE;
		String commentType = isReplyComment ? "답글" : "댓글";
		String secretStringRemovedContent = MentionSecretStringRemover.removeSecretString(requestBody.getContents());
		String pushNotificationContent = String.format("[%s의 %s] : \"%s\"",
			writerProfile.getAnonymousNickname(), commentType, secretStringRemovedContent);
		String webLink = createMeetingDemandWebLink(meetingDemand.getId());

		PushNotificationRequestDto pushRequestDto = PushNotificationRequestDto.of(
			new String[] {String.valueOf(receiverUserId)},
			title,
			pushNotificationContent,
			PUSH_NOTIFICATION_CATEGORY.getValue(),
			webLink
		);

		pushNotificationService.sendPushNotification(pushRequestDto);
	}

	public void sendMentionNotification(MeetingDemandCommentV2MentionUserInCommentRequestDto requestBody,
		MeetingDemandCommentProfile writerProfile) {
		String title = String.format(MEETING_DEMAND_MENTION_TITLE_FORMAT, writerProfile.getAnonymousNickname());
		String content = "\"" + MentionSecretStringRemover.removeSecretString(requestBody.getContent()) + "\"";
		String webLink = createMeetingDemandWebLink(requestBody.getMeetingDemandId());

		String[] userOrgIds = requestBody.getOrgIds().stream()
			.map(Object::toString)
			.toArray(String[]::new);

		PushNotificationRequestDto pushRequestDto = PushNotificationRequestDto.of(
			userOrgIds,
			title,
			content,
			PUSH_NOTIFICATION_CATEGORY.getValue(),
			webLink
		);

		pushNotificationService.sendPushNotification(pushRequestDto);
	}

	private Integer getReplyReceiverUserId(MeetingDemandComment parentComment) {
		if (parentComment == null) {
			return null;
		}

		return parentComment.getUserId();
	}

	private String createMeetingDemandWebLink(Integer meetingDemandId) {
		return String.format(MEETING_DEMAND_WEB_LINK_FORMAT, pushNotificationProperties.getPushWebUrl(),
			meetingDemandId);
	}
}
