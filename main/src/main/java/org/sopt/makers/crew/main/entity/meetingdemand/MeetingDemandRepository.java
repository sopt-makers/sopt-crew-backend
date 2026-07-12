package org.sopt.makers.crew.main.entity.meetingdemand;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.NOT_FOUND_MEETING_DEMAND;

import java.util.Optional;

import org.sopt.makers.crew.main.entity.meetingdemand.enums.MeetingDemandStatus;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface MeetingDemandRepository extends JpaRepository<MeetingDemand, Integer> {

	Page<MeetingDemand> findAllByStatus(MeetingDemandStatus status, Pageable pageable);

	int countByStatus(MeetingDemandStatus status);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select md from MeetingDemand md where md.id = :meetingDemandId")
	Optional<MeetingDemand> findByIdWithPessimisticWriteLock(@Param("meetingDemandId") Integer meetingDemandId);

	default MeetingDemand findByIdOrThrow(Integer meetingDemandId) {
		return findById(meetingDemandId)
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_MEETING_DEMAND.getErrorCode()));
	}

	default MeetingDemand findByIdWithPessimisticWriteLockOrThrow(Integer meetingDemandId) {
		return findByIdWithPessimisticWriteLock(meetingDemandId)
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_MEETING_DEMAND.getErrorCode()));
	}
}
