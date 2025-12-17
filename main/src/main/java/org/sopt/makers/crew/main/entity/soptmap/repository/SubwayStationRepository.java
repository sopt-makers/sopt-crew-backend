package org.sopt.makers.crew.main.entity.soptmap.repository;

import java.util.List;

import org.sopt.makers.crew.main.entity.soptmap.SubwayStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubwayStationRepository extends JpaRepository<SubwayStation, Long> {

	List<SubwayStation> findAllByNameIn(List<String> stationNames);

	List<SubwayStation> findSubwayStationByNameContaining(String name);

	@Query(value = """
		SELECT *
		FROM subway_station
		WHERE name ILIKE CONCAT('%', :keyword, '%')
		ORDER BY
			CASE
				WHEN name = :keyword THEN 0
				WHEN name ILIKE CONCAT(:keyword, '%') THEN 1
				ELSE 2
			END,
			public.similarity(name, :keyword) DESC
		LIMIT 5
		""", nativeQuery = true)
	List<SubwayStation> searchByKeyword(@Param("keyword") String keyword);
}

