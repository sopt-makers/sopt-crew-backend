package org.sopt.makers.crew.main.meeting.v2.service;

import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyType;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.meeting.v2.dto.ApplyMapper;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2ApplyMeetingDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Apply 저장을 위한 트랜잭션 전용 서비스
 * <p>
 * MeetingV2ServiceImpl에서 트랜잭션 범위를 축소하기 위해 분리됨.
 * Self-invocation 문제를 해결하고, 쓰기 작업만 트랜잭션 내에서 실행.
 */
@Service
@RequiredArgsConstructor
public class ApplyTransactionService {

	private final ApplyRepository applyRepository;
	private final ApplyMapper applyMapper;

	/**
	 * Apply 엔티티를 생성하고 저장
	 * <p>
	 * REQUIRES_NEW: 외부 readOnly 트랜잭션과 무관하게 독립적인 쓰기 트랜잭션 시작
	 * Connection을 2번만 획득: 읽기 1번(외부) + 쓰기 1번(이 메서드)
	 *
	 * @param requestBody 신청 요청 DTO
	 * @param applyType   신청 유형
	 * @param meeting     모임 엔티티
	 * @param user        사용자 엔티티
	 * @param userId      사용자 ID
	 * @return 저장된 Apply 엔티티
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Apply saveApply(MeetingV2ApplyMeetingDto requestBody,
			EnApplyType applyType,
			Meeting meeting,
			User user,
			Integer userId) {
		Apply apply = applyMapper.toApplyEntity(requestBody, applyType, meeting, user, userId);
		return applyRepository.save(apply);
	}
}
