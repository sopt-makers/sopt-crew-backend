package org.sopt.makers.crew.main.entity.slack;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "makers_user_slack")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
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
}
