package org.sopt.makers.crew.main.soptmap.service.dto;

import org.sopt.makers.crew.main.entity.soptmap.EventGift;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventGiftDto {

	private Long id;
	private String giftUrl;
	private Long userId;
	private Long mapId;

	public static EventGiftDto from(EventGift eventGift) {
		return new EventGiftDto(eventGift.getId(),
			eventGift.getGiftUrl(), eventGift.getUserId(), eventGift.getMapId());
	}
}
