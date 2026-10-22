package org.sopt.makers.crew.main.dining.v2.service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.sopt.makers.crew.main.entity.dining.DiningAttendance;
import org.sopt.makers.crew.main.entity.dining.DiningDeletionHistory;
import org.sopt.makers.crew.main.entity.dining.DiningDeletionHistoryRepository;
import org.sopt.makers.crew.main.entity.dining.DiningRound;
import org.sopt.makers.crew.main.entity.dining.enums.DiningDeletionTargetType;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 회식, 회차, 참석 기록 삭제 시 삭제 직전 데이터를 스냅샷으로 보관한다.
 * 원본 삭제 전에 같은 트랜잭션 안에서 호출해야 한다.
 * 시간, enum 값은 jsonb 직렬화 방식에 의존하지 않도록 문자열로 저장한다.
 */
@Component
@RequiredArgsConstructor
public class DiningDeletionHistoryRecorder {

	private final DiningDeletionHistoryRepository diningDeletionHistoryRepository;

	/**
	 * 회식 삭제: 회식 + 전체 회차 + 각 회차의 참석 기록
	 */
	public void recordDiningDeletion(Meeting meeting, List<DiningRound> rounds, List<DiningAttendance> attendances,
		Integer deletedByUserId) {
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("meetingId", meeting.getId());
		data.put("leaderUserId", meeting.getUserId());
		data.put("title", meeting.getTitle());
		data.put("imageUrls", toImageUrls(meeting.getImageURL()));
		data.put("diningDate", toText(meeting.getmStartDate() == null ? null : meeting.getmStartDate().toLocalDate()));
		data.put("joinableParts", toPartNames(meeting));
		data.put("diningEndedAt", toText(meeting.getDiningEndedAt()));
		data.put("isDateModified", meeting.isDateModified());
		data.put("createdGeneration", meeting.getCreatedGeneration());
		data.put("createdTimestamp", toText(meeting.createdTimestamp));
		data.put("rounds", rounds.stream()
			.map(round -> toRoundData(round, filterAttendances(attendances, round)))
			.toList());

		save(meeting.getId(), DiningDeletionTargetType.DINING, meeting.getId(), deletedByUserId, data);
	}

	/**
	 * 회차 삭제: 회차 + 그 회차의 참석 기록
	 */
	public void recordRoundDeletion(DiningRound round, List<DiningAttendance> attendances, Integer deletedByUserId) {
		save(round.getMeetingId(), DiningDeletionTargetType.ROUND, round.getId(), deletedByUserId,
			toRoundData(round, attendances));
	}

	/**
	 * 참석 기록 삭제: 신청취소, 참석자 관리에서 삭제
	 */
	public void recordAttendanceDeletion(DiningRound round, DiningAttendance attendance, Integer deletedByUserId) {
		save(round.getMeetingId(), DiningDeletionTargetType.ATTENDANCE, attendance.getId(), deletedByUserId,
			toAttendanceData(attendance));
	}

	private void save(Integer meetingId, DiningDeletionTargetType targetType, Integer targetId,
		Integer deletedByUserId, Map<String, Object> data) {
		diningDeletionHistoryRepository.save(DiningDeletionHistory.builder()
			.meetingId(meetingId)
			.targetType(targetType)
			.targetId(targetId)
			.deletedByUserId(deletedByUserId)
			.data(data)
			.build());
	}

	private Map<String, Object> toRoundData(DiningRound round, List<DiningAttendance> attendances) {
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("roundId", round.getId());
		data.put("meetingId", round.getMeetingId());
		data.put("place", round.getPlace());
		data.put("naverMapLink", round.getNaverMapLink());
		data.put("kakaoMapLink", round.getKakaoMapLink());
		data.put("startTime", toText(round.getStartTime()));
		data.put("status", round.getStatus().name());
		data.put("closedAt", toText(round.getClosedAt()));
		data.put("isPlaceModified", round.isPlaceModified());
		data.put("isLinkModified", round.isLinkModified());
		data.put("isStartTimeModified", round.isStartTimeModified());
		data.put("createdTimestamp", toText(round.createdTimestamp));
		data.put("attendances", attendances.stream()
			.map(this::toAttendanceData)
			.toList());
		return data;
	}

	private Map<String, Object> toAttendanceData(DiningAttendance attendance) {
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("attendanceId", attendance.getId());
		data.put("roundId", attendance.getRoundId());
		data.put("userId", attendance.getUserId());
		data.put("payerStatus", attendance.getPayerStatus() == null ? null : attendance.getPayerStatus().name());
		data.put("payerRequestedAt", toText(attendance.getPayerRequestedAt()));
		data.put("createdTimestamp", toText(attendance.createdTimestamp));
		return data;
	}

	private List<DiningAttendance> filterAttendances(List<DiningAttendance> attendances, DiningRound round) {
		return attendances.stream()
			.filter(attendance -> round.getId().equals(attendance.getRoundId()))
			.toList();
	}

	private List<String> toImageUrls(List<ImageUrlVO> imageUrls) {
		if (imageUrls == null) {
			return List.of();
		}
		return imageUrls.stream()
			.map(ImageUrlVO::getUrl)
			.toList();
	}

	private List<String> toPartNames(Meeting meeting) {
		if (meeting.getJoinableParts() == null) {
			return List.of();
		}
		return Arrays.stream(meeting.getJoinableParts())
			.map(Enum::name)
			.toList();
	}

	private String toText(Object value) {
		return Objects.toString(value, null);
	}
}
