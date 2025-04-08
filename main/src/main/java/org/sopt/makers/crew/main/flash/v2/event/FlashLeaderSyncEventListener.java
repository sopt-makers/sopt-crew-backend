package org.sopt.makers.crew.main.flash.v2.event;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.Objects;

import org.sopt.makers.crew.main.entity.flash.Flash;
import org.sopt.makers.crew.main.entity.flash.FlashRepository;
import org.sopt.makers.crew.main.flash.v2.dto.event.FlashLeaderSyncEventDto;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.sopt.makers.crew.main.meeting.v2.service.MeetingV2Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlashLeaderSyncEventListener {

	private final MeetingV2Service meetingV2Service;
	private final FlashRepository flashRepository;

	@Async("taskExecutor")
	@Transactional
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleFlashLeaderSyncEvent(FlashLeaderSyncEventDto event) {
		log.info("FlashLeaderSyncEvent 수신 - meetingId: {}, oldLeaderId: {}, newLeaderId: {}",
			event.meetingId(), event.oldLeaderUserId(), event.newLeaderUserId());

		try {
			synchronizeFlashLeader(event);
		} catch (Exception e) {
			log.error("Flash 리더 동기화 중 오류 발생 - meetingId: {}", event.meetingId(), e);
		}
	}

	private void synchronizeFlashLeader(FlashLeaderSyncEventDto event) {
		Flash flash = flashRepository.findByMeetingId(event.meetingId())
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_FLASH.getErrorCode()));

		if (!Objects.equals(flash.getLeaderUserId(), event.newLeaderUserId())) {
			flash.updateLeaderUserId(event.newLeaderUserId());
			flashRepository.save(flash);

			meetingV2Service.evictMeetingCache(event.meetingId());
			meetingV2Service.evictLeaderCache(event.oldLeaderUserId());
			meetingV2Service.evictLeaderCache(event.newLeaderUserId());

			log.info("Flash 리더 업데이트 완료 - meetingId: {}, newLeaderId: {}",
				event.meetingId(), event.newLeaderUserId());
		} else {
			log.info("이미 리더가 동기화되어 있습니다 - meetingId: {}, leaderId: {}",
				event.meetingId(), event.newLeaderUserId());
		}
	}
}
