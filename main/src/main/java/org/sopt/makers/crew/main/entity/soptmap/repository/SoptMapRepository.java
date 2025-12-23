package org.sopt.makers.crew.main.entity.soptmap.repository;

import org.sopt.makers.crew.main.entity.soptmap.SoptMap;
import org.sopt.makers.crew.main.entity.soptmap.repository.querydsl.SoptMapQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoptMapRepository extends JpaRepository<SoptMap, Long>, SoptMapQueryRepository {
	boolean existsByPlaceName(String placeName);
}
