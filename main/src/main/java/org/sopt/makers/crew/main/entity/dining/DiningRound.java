package org.sopt.makers.crew.main.entity.dining;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;
import org.sopt.makers.crew.main.entity.dining.enums.DiningRoundStatus;
import org.sopt.makers.crew.main.global.exception.BadRequestException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회식 회차 (1차, 2차, ...)
 * 회차 번호는 저장하지 않고 같은 회식(meetingId) 내 id 오름차순으로 계산한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "dining_round")
public class DiningRound extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	/**
	 * 회식(meeting) id
	 */
	@Column(nullable = false)
	private Integer meetingId;

	/**
	 * 회식 장소 (null 이면 장소 미정)
	 */
	@Column(length = 30)
	private String place;

	/**
	 * 네이버지도 링크
	 */
	@Column(length = 500)
	private String naverMapLink;

	/**
	 * 카카오맵 링크
	 */
	@Column(length = 500)
	private String kakaoMapLink;

	/**
	 * 시작 시간 (null 이면 미노출)
	 */
	@Column(columnDefinition = "TIME")
	private LocalTime startTime;

	/**
	 * 회차 상태 (진행중 / 마감)
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DiningRoundStatus status;

	/**
	 * 마감 시각
	 */
	@Column(columnDefinition = "TIMESTAMP")
	private LocalDateTime closedAt;

	/**
	 * 장소 수정 여부
	 */
	@Column(nullable = false)
	private boolean isPlaceModified;

	/**
	 * 장소 링크 수정 여부 (네이버지도, 카카오맵 공통)
	 */
	@Column(nullable = false)
	private boolean isLinkModified;

	/**
	 * 시작 시간 수정 여부
	 */
	@Column(nullable = false)
	private boolean isStartTimeModified;

	@Builder
	public DiningRound(Integer meetingId, String place, String naverMapLink, String kakaoMapLink,
		LocalTime startTime) {
		this.meetingId = meetingId;
		this.place = place;
		this.naverMapLink = naverMapLink;
		this.kakaoMapLink = kakaoMapLink;
		this.startTime = startTime;
		this.status = DiningRoundStatus.OPEN;
		this.isPlaceModified = false;
		this.isLinkModified = false;
		this.isStartTimeModified = false;
	}

	/**
	 * 회차 정보 수정. 값이 실제로 바뀐 항목만 수정됨 표시를 남긴다.
	 * 마감된 회차도 수정할 수 있다.
	 */
	public void update(String place, String naverMapLink, String kakaoMapLink, LocalTime startTime) {
		if (!Objects.equals(this.place, place)) {
			this.isPlaceModified = true;
		}
		if (!Objects.equals(this.naverMapLink, naverMapLink) || !Objects.equals(this.kakaoMapLink, kakaoMapLink)) {
			this.isLinkModified = true;
		}
		if (!Objects.equals(this.startTime, startTime)) {
			this.isStartTimeModified = true;
		}

		this.place = place;
		this.naverMapLink = naverMapLink;
		this.kakaoMapLink = kakaoMapLink;
		this.startTime = startTime;
	}

	public void close(LocalDateTime now) {
		validateNotClosed();
		this.status = DiningRoundStatus.CLOSED;
		this.closedAt = now;
	}

	public void validateNotClosed() {
		if (isClosed()) {
			throw new BadRequestException(ALREADY_CLOSED_DINING_ROUND.getErrorCode());
		}
	}

	public boolean isClosed() {
		return this.status == DiningRoundStatus.CLOSED;
	}

	/**
	 * "수정된 정보가 있어요" 표시 여부. 한 번 수정되면 계속 표시한다.
	 */
	public boolean hasModifiedInfo() {
		return this.isPlaceModified || this.isLinkModified || this.isStartTimeModified;
	}
}
