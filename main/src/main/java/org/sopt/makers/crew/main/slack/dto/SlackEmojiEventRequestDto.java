package org.sopt.makers.crew.main.slack.dto;

import org.sopt.makers.crew.main.slack.dto.SlackEmojiEventDto.SlackDeleteEmojiEventDto;
import org.sopt.makers.crew.main.slack.dto.SlackEmojiEventDto.SlackUpdateEmojiEventDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SlackEmojiEventRequestDto {

	private String identifiedPwd;
	private String callEmoji;
	private String username;
	private String userSlackId;
	private String team;
	private Integer generation;
	private String templateCd;

	public SlackEmojiEventDto toDto() {
		return SlackEmojiEventDto.builder()
			.callEmoji(callEmoji)
			.username(username)
			.userSlackId(userSlackId)
			.team(team)
			.generation(generation)
			.templateCd(templateCd)
			.build();
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	public static class SlackUpdateEmojiEventRequestDto {

		private String identifiedPwd;
		private String originalCallEmoji;
		private String username;
		private String userSlackId;
		private String team;
		private Integer generation;
		private String updateCallEmoji;

		public SlackUpdateEmojiEventDto toDto() {
			return SlackUpdateEmojiEventDto.builder()
				.callEmoji(originalCallEmoji)
				.updateCallEmoji(updateCallEmoji)
				.build();
		}
	}


	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class SlackEmojiEventDeleteRequestDto {

		private String identifiedPwd;
		private String callEmoji;

		public SlackDeleteEmojiEventDto toDto(){
			return SlackDeleteEmojiEventDto.builder()
				.callEmoji(callEmoji)
				.build();
		}

	}
}
