package org.sopt.makers.crew.main.entity.lightning;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LightningRepository extends JpaRepository<Lightning, Integer> {
	Optional<Lightning> findByMeetingId(Integer meetingId);
}
