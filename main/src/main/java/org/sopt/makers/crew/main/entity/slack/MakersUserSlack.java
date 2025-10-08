package org.sopt.makers.crew.main.entity.slack;

import org.sopt.makers.crew.main.slack.dto.SlackEmojiEventDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "makers_user_slack")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class MakersUserSlack {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "username")
	private String username; // 이름

	@Column(name = "user_slack_id")
	private String userSlackId; // 유저 slack Id

	@Column(name = "team")
	private String team; // 유저 팀

	@Column(name = "generation")
	private Integer generation; // 기수

	@Column(name = "call_emoji")
	private String callEmoji; // 호출 이모지

	@Column(name = "slack_message_template_cd")
	private String slackTemplateCd;

	public static MakersUserSlack create(SlackEmojiEventDto dto) {
		return MakersUserSlack.builder()
			.username(dto.getUsername())
			.userSlackId(dto.getUserSlackId())
			.team(dto.getTeam())
			.generation(dto.getGeneration())
			.callEmoji(dto.getCallEmoji())
			.slackTemplateCd(dto.getTemplateCd())
			.build();
	}

	public void updateEmoji(String callEmoji) {
		this.callEmoji = callEmoji;
	}
}
