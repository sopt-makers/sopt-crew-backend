package org.sopt.makers.crew.main.entity.dining;

import java.util.LinkedHashMap;
import java.util.Map;

import org.hibernate.annotations.Type;
import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;
import org.sopt.makers.crew.main.entity.dining.enums.DiningDeletionTargetType;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
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
 * 회식, 회차, 참석 기록 삭제 시 보관하는 스냅샷
 * 원본 삭제 이후에도 남아야 하므로 FK 를 두지 않는다.
 * snapshot 형식: { "schemaVersion": 1, "data": { ... } }
 * 엔티티 구조가 바뀌어 snapshot 의 data 형식이 달라지면 SNAPSHOT_SCHEMA_VERSION 을 올린다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "dining_deletion_history")
public class DiningDeletionHistory extends BaseTimeEntity {

	public static final int SNAPSHOT_SCHEMA_VERSION = 1;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	/**
	 * 회식(meeting) id
	 */
	@Column(nullable = false)
	private Integer meetingId;

	/**
	 * 삭제 대상 종류 (회식 / 회차 / 참석)
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DiningDeletionTargetType targetType;

	/**
	 * 삭제 대상 id (meeting / dining_round / dining_attendance 의 id)
	 */
	@Column(nullable = false)
	private Integer targetId;

	/**
	 * 삭제한 유저 id
	 */
	@Column(nullable = false)
	private Integer deletedByUserId;

	/**
	 * 삭제 직전 데이터 스냅샷 (schemaVersion + data)
	 */
	@Column(nullable = false, columnDefinition = "jsonb")
	@Type(JsonBinaryType.class)
	private Map<String, Object> snapshot;

	@Builder
	public DiningDeletionHistory(Integer meetingId, DiningDeletionTargetType targetType, Integer targetId,
		Integer deletedByUserId, Map<String, Object> data) {
		this.meetingId = meetingId;
		this.targetType = targetType;
		this.targetId = targetId;
		this.deletedByUserId = deletedByUserId;
		this.snapshot = wrapSnapshot(data);
	}

	private static Map<String, Object> wrapSnapshot(Map<String, Object> data) {
		Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("schemaVersion", SNAPSHOT_SCHEMA_VERSION);
		snapshot.put("data", data);
		return snapshot;
	}
}
