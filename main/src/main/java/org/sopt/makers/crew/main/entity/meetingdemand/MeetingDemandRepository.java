package org.sopt.makers.crew.main.entity.meetingdemand;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.NOT_FOUND_MEETING_DEMAND;

import org.sopt.makers.crew.main.entity.meetingdemand.enums.MeetingDemandStatus;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingDemandRepository extends JpaRepository<MeetingDemand, Integer> {

	Page<MeetingDemand> findAllByStatus(MeetingDemandStatus status, Pageable pageable);

	int countByStatus(MeetingDemandStatus status);

	default MeetingDemand findByIdOrThrow(Integer meetingDemandId) {
		return findById(meetingDemandId)
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_MEETING_DEMAND.getErrorCode()));
	}
}
