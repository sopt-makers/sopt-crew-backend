package org.sopt.makers.crew.main.external.playground.entity.member_block;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_block")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberBlock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	@NotNull
	@Column(name = "is_blocked")
	private Boolean isBlocked = true;
	@Column(name = "blocked_id")
	private Long blockedId;
	@Column(name = "blocker_id")
	private Long blockerId;

}
