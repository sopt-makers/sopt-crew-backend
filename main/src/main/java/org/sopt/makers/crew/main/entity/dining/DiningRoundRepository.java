package org.sopt.makers.crew.main.entity.dining;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.List;

import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiningRoundRepository extends JpaRepository<DiningRound, Integer> {

	default DiningRound findByIdOrThrow(Integer roundId) {
		return findById(roundId)
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_DINING_ROUND.getErrorCode()));
	}

	/**
	 * @implSpec : 회식의 회차를 1차부터 순서대로 조회한다. (인덱스 + 1 = 회차 번호)
	 */
	List<DiningRound> findAllByMeetingIdOrderByIdAsc(Integer meetingId);

	List<DiningRound> findAllByMeetingIdInOrderByIdAsc(List<Integer> meetingIds);
}
