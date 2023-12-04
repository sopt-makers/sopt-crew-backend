package org.sopt.makers.crew.main.entity.apply;

import java.util.List;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApplyRepository extends JpaRepository<Apply, Integer> {

  @Query("select a from Apply a join fetch a.meeting m where a.userId = :userId and a.status = :statusValue")
  List<Apply> findAllByUserIdAndStatus(@Param("userId") Integer userId,
      @Param("statusValue") EnApplyStatus statusValue);

}
