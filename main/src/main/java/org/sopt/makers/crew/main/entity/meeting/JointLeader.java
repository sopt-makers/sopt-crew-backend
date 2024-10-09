package org.sopt.makers.crew.main.entity.meeting;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JointLeader {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@NotNull
	private Integer meetingId;

	@NotNull
	private Integer userId;

	@Builder
	private JointLeader(Integer meetingId, Integer userId) {
		this.meetingId = meetingId;
		this.userId = userId;
	}
}
