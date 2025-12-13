package org.sopt.makers.crew.main.entity.soptmap.repository;

import org.sopt.makers.crew.main.entity.soptmap.SoptMap;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoptMapRepository extends JpaRepository<SoptMap, Long> {
	boolean existsByPlaceName(String placeName);
}
