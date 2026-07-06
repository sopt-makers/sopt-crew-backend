package org.sopt.makers.crew.main.meetingdemand.v2.service;

import java.time.LocalDateTime;

import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.EnMeetingStatus;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandOpenedNotification;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandOpenedNotificationRepository;
import org.sopt.makers.crew.main.external.notification.event.NotificationTimeValidator;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.event.MeetingDemandOpenedNotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeetingDemandOpenedNotificationService {

	private final MeetingDemandOpenedNotificationRepository meetingDemandOpenedNotificationRepository;
	private final MeetingRepository meetingRepository;
	private final MeetingDemandNotificationSender meetingDemandNotificationSender;
	private final ApplicationEventPublisher eventPublisher;
	private final Time time;

	@Transactional
	public void register(Meeting meeting) {
		if (meeting.getMeetingDemandId() == null) {
			return;
		}

		LocalDateTime now = time.now();
		EnMeetingStatus meetingStatus = meeting.getMeetingStatus(now);
		if (EnMeetingStatus.RECRUITMENT_COMPLETE.equals(meetingStatus)) {
			return;
		}

		MeetingDemandOpenedNotification notification = findOrCreate(meeting.getId());
		if (!notification.isSent()
			&& EnMeetingStatus.APPLY_ABLE.equals(meetingStatus)
			&& NotificationTimeValidator.isPublishedTime(now)) {
			eventPublisher.publishEvent(new MeetingDemandOpenedNotificationEvent(meeting.getId()));
		}
	}

	@Transactional
	public void sendNotification(Integer meetingId) {
		LocalDateTime now = time.now();
		if (!NotificationTimeValidator.isPublishedTime(now)) {
			return;
		}

		MeetingDemandOpenedNotification notification = meetingDemandOpenedNotificationRepository
			.findByMeetingId(meetingId)
			.orElse(null);
		if (notification == null || notification.isSent()) {
			return;
		}

		Meeting meeting = meetingRepository.findByIdOrThrow(meetingId);
		if (!EnMeetingStatus.APPLY_ABLE.equals(meeting.getMeetingStatus(now))) {
			return;
		}

		meetingDemandNotificationSender.sendOpenedMeetingNotification(meeting);
		notification.markSent(now);
	}

	@Transactional
	public void sendPendingNotifications() {
		LocalDateTime now = time.now();
		if (!NotificationTimeValidator.isPublishedTime(now)) {
			return;
		}

		meetingDemandOpenedNotificationRepository.findAllUnsentApplyAble(now)
			.forEach(notification -> sendNotification(notification.getMeetingId()));
	}

	private MeetingDemandOpenedNotification findOrCreate(Integer meetingId) {
		return meetingDemandOpenedNotificationRepository.findByMeetingId(meetingId)
			.orElseGet(() -> meetingDemandOpenedNotificationRepository.save(
				MeetingDemandOpenedNotification.builder()
					.meetingId(meetingId)
					.build()
			));
	}
}
