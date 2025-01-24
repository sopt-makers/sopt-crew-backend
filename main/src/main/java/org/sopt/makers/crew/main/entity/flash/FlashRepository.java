package org.sopt.makers.crew.main.entity.flash;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashRepository extends JpaRepository<Flash, Integer> {
	Optional<Flash> findByMeetingId(Integer meetingId);
}
