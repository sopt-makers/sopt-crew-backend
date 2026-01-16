package org.sopt.makers.crew.main.entity.soptmap.repository;

import java.util.Optional;

import org.sopt.makers.crew.main.entity.soptmap.EventGift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

public interface EventGiftRepository extends JpaRepository<EventGift, Long> {

	boolean existsByUserId(long userId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT eg FROM EventGift eg WHERE eg.claimable = true AND eg.active = true order by eg.id limit 1")
	Optional<EventGift> findFirstClaimableGiftWithLock();
}