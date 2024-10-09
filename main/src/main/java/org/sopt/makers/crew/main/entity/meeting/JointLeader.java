package org.sopt.makers.crew.main.entity.meeting;

import org.sopt.makers.crew.main.entity.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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

	@ManyToOne(fetch = FetchType.LAZY)
	@NotNull
	private Meeting meeting;

	@ManyToOne(fetch = FetchType.LAZY)
	@NotNull
	private User user;

	@Builder
	private JointLeader(Meeting meeting, User user) {
		this.meeting = meeting;
		this.user = user;
	}
}
