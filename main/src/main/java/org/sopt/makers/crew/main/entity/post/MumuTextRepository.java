package org.sopt.makers.crew.main.entity.post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MumuTextRepository extends JpaRepository<MumuText, Long> {

	@Query("select m from MumuText  m where :dateTime >= m.showStartDate and :dateTime <= m.showEndDate")
	Optional<MumuText> findByInDateTimeText(@Param("dateTime") LocalDateTime dateTime);

	List<MumuText> findAllByOrderByShowStartDateAsc();
}
