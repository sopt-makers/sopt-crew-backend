package org.sopt.makers.crew.main.entity.soptmap.repository;

import java.util.List;

import org.sopt.makers.crew.main.entity.soptmap.SubwayStation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubwayStationRepository extends JpaRepository<SubwayStation, Long> {

	List<SubwayStation> findAllByNameIn(List<String> stationNames);
}
