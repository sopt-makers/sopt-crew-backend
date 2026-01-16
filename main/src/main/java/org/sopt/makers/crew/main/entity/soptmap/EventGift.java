package org.sopt.makers.crew.main.entity.soptmap;

import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "event_gift")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventGift extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "map_id")
	private Long mapId;

	@Column(name = "gift_url")
	private String giftUrl;

	@Column(name = "claimable") //청구 수령 가능한
	private Boolean claimable;

	@Column(name = "active")
	private Boolean active;

	public void nonActive() {
		this.active = false;
	}

	public void claimGift(Integer userId, Long mapId) {
		this.userId = Long.valueOf(userId);
		this.mapId = mapId;
		this.claimable = false;
	}

	public void claimGift(Long userId, Long mapId) {
		this.userId = userId;
		this.mapId = mapId;
		this.claimable = false;
	}
}
