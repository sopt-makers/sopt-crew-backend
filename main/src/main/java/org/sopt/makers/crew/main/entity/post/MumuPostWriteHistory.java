package org.sopt.makers.crew.main.entity.post;

import java.time.LocalDate;

import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "mumu_post_write_history",
	uniqueConstraints = @UniqueConstraint(
		name = "UQ_mumu_post_write_history_user_written_date",
		columnNames = {"userId", "writtenDate"}
	))
public class MumuPostWriteHistory extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private Integer userId;

	@Column(nullable = false)
	private LocalDate writtenDate;

	@Builder
	public MumuPostWriteHistory(Integer userId, LocalDate writtenDate) {
		this.userId = userId;
		this.writtenDate = writtenDate;
	}
}
