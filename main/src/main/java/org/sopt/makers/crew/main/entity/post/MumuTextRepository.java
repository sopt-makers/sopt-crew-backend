package org.sopt.makers.crew.main.entity.post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MumuTextRepository extends JpaRepository<MumuText, Long> {

	default MumuText findByIdOrThrow(Long mumuTextId) {
		return findById(mumuTextId)
			.orElseThrow(() -> new BadRequestException("무무씨 질문이 없습니다."));
	}

	@Query("select m from MumuText m where :dateTime >= m.showStartDate and :dateTime < m.showEndDate")
	Optional<MumuText> findByInDateTimeText(@Param("dateTime") LocalDateTime dateTime);

	List<MumuText> findAllByOrderByShowStartDateAsc();

	List<MumuText> findAllByOrderByShowStartDateAscIdAsc();

	@Query("select m from MumuText m " +
		"where m.showStartDate < :showEndDate " +
		"and m.showEndDate > :showStartDate " +
		"order by m.showStartDate asc, m.id asc")
	List<MumuText> findOverlappingMumuTexts(
		@Param("showStartDate") LocalDateTime showStartDate,
		@Param("showEndDate") LocalDateTime showEndDate);

	@Query("select m from MumuText m " +
		"where m.id <> :mumuTextId " +
		"and m.showStartDate < :showEndDate " +
		"and m.showEndDate > :showStartDate " +
		"order by m.showStartDate asc, m.id asc")
	List<MumuText> findOverlappingMumuTextsExcludingId(
		@Param("mumuTextId") Long mumuTextId,
		@Param("showStartDate") LocalDateTime showStartDate,
		@Param("showEndDate") LocalDateTime showEndDate);
}
