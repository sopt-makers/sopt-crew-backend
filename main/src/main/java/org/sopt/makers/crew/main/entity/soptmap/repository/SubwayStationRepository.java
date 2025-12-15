package org.sopt.makers.crew.main.entity.soptmap.repository;

import java.util.List;

import org.sopt.makers.crew.main.entity.soptmap.SubwayStation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface SubwayStationRepository extends CrudRepository<SubwayStation, Long> {

	@Query("select st.id from SubwayStation st where st.name in :stationNames")
	List<Long> findIdsByStationNames(@Param("stationNames") List<String> stationNames);
}

