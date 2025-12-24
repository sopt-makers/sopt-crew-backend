package org.sopt.makers.crew.main.entity.soptmap.repository;

import org.sopt.makers.crew.main.entity.soptmap.MapRecommend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MapRecommendRepository extends JpaRepository<MapRecommend, Long> {
    @Modifying(clearAutomatically = true)
    @Query("delete from MapRecommend m where m.soptMapId = :soptMapId")
    void deleteAllBySoptMapId(@Param("soptMapId") Long soptMapId);
}
