package org.sopt.makers.crew.main.entity.dining;

import java.util.List;

import org.sopt.makers.crew.main.entity.dining.enums.DiningDeletionTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiningDeletionHistoryRepository extends JpaRepository<DiningDeletionHistory, Integer> {

	List<DiningDeletionHistory> findAllByMeetingIdAndTargetTypeIn(Integer meetingId,
		List<DiningDeletionTargetType> targetTypes);
}
