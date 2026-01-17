package org.sopt.makers.crew.main.soptmap.service;

import org.sopt.makers.crew.main.entity.soptmap.EventGift;
import org.sopt.makers.crew.main.entity.soptmap.repository.querydsl.EventGiftQueryRepository;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.soptmap.service.dto.EventGiftDto;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventGiftService {

	private final EventGiftQueryRepository eventGiftQueryRepository;

	@Transactional
	public EventGiftDto retrieveEventGift(Integer userId, Long mapId) {
		EventGift eventGift = eventGiftQueryRepository.findByUserId(Long.valueOf(userId), mapId)
			.orElseThrow(() -> new BadRequestException("Could not find event gift for user " + userId));

		eventGift.nonActive();

		return EventGiftDto.from(eventGift);
	}
}
