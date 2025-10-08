package org.sopt.makers.crew.main.slack.dto;

import org.sopt.makers.crew.main.entity.slack.MakersUserSlack;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class SlackEmojiEventDto {

	private String callEmoji;
	private String username;
	private String userSlackId;
	private String team;
	private Integer generation;
	private String templateCd;

	public MakersUserSlack toEntity() {
		return MakersUserSlack.create(this);
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class SlackUpdateEmojiEventDto {
		private String callEmoji;
		private String updateCallEmoji;
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class SlackDeleteEmojiEventDto {
		private String callEmoji;
	}

}
