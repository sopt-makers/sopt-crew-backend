package org.sopt.makers.crew.main.entity.dining;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.time.LocalDateTime;

import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;
import org.sopt.makers.crew.main.entity.dining.enums.DiningPayerStatus;
import org.sopt.makers.crew.main.global.exception.BadRequestException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회차별 참석 기록 + 공동 결제자 요청 상태
 * 미참석은 행이 없는 상태로 표현한다. (신청취소, 참석자 삭제 시 행 삭제)
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "dining_attendance",
	uniqueConstraints = @UniqueConstraint(
		name = "UQ_dining_attendance_round_user",
		columnNames = {"roundId", "userId"}
	))
public class DiningAttendance extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	/**
	 * 회차 id
	 */
	@Column(nullable = false)
	private Integer roundId;

	/**
	 * 참석자 id
	 */
	@Column(nullable = false)
	private Integer userId;

	/**
	 * 공동 결제자 요청 상태 (null 이면 요청 없음)
	 */
	@Enumerated(EnumType.STRING)
	private DiningPayerStatus payerStatus;

	/**
	 * 공동 결제자 요청 시각
	 */
	@Column(columnDefinition = "TIMESTAMP")
	private LocalDateTime payerRequestedAt;

	@Builder
	public DiningAttendance(Integer roundId, Integer userId) {
		this.roundId = roundId;
		this.userId = userId;
	}

	public void requestPayer(LocalDateTime now) {
		if (this.payerStatus != null) {
			throw new BadRequestException(ALREADY_REQUESTED_DINING_PAYER.getErrorCode());
		}
		this.payerStatus = DiningPayerStatus.PENDING;
		this.payerRequestedAt = now;
	}

	public void approvePayer() {
		validatePendingPayer();
		this.payerStatus = DiningPayerStatus.APPROVED;
	}

	/**
	 * 결제자 요청 거절 또는 승인 해제. 참석은 유지한다.
	 */
	public void clearPayer() {
		this.payerStatus = null;
		this.payerRequestedAt = null;
	}

	public void validatePendingPayer() {
		if (this.payerStatus != DiningPayerStatus.PENDING) {
			throw new BadRequestException(NOT_PENDING_DINING_PAYER.getErrorCode());
		}
	}

	public void validateApprovedPayer() {
		if (!isApprovedPayer()) {
			throw new BadRequestException(NOT_APPROVED_DINING_PAYER.getErrorCode());
		}
	}

	public boolean isApprovedPayer() {
		return this.payerStatus == DiningPayerStatus.APPROVED;
	}
}
