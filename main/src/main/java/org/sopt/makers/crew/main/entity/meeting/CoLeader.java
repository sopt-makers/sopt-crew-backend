package org.sopt.makers.crew.main.entity.meeting;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.Objects;

import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.global.exception.BadRequestException;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "co_leader")
public class CoLeader extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "meetingId")
	@NotNull
	private Meeting meeting;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userId")
	@NotNull
	private User user;

	@Builder
	private CoLeader(Meeting meeting, User user) {
		if (Objects.equals(meeting.getUserId(), user.getId())) {
			throw new BadRequestException(LEADER_CANNOT_BE_CO_LEADER_APPLY.getErrorCode());
		}
		this.meeting = meeting;
		this.user = user;
	}
}
