package org.sopt.makers.crew.main.entity.soptmap.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.sopt.makers.crew.main.entity.soptmap.SoptMap;
import org.sopt.makers.crew.main.entity.soptmap.repository.querydsl.SoptMapQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SoptMapRepository extends JpaRepository<SoptMap, Long>, SoptMapQueryRepository {
	boolean existsByPlaceName(String placeName);

	boolean existsByCreatorId(Long creatorId);

	@Query(value = """
		select * from sopt_map
		where "createdTimestamp" >= :startDate and "createdTimestamp" <= :endDate
		order by id asc
		""", nativeQuery = true)
	List<SoptMap> findFirstEventSoptMaps(LocalDateTime startDate, LocalDateTime endDate);

	boolean existsSoptMapByCreatorIdAndId(Long userId, Long id);
}
