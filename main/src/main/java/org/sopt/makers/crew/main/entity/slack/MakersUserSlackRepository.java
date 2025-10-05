package org.sopt.makers.crew.main.entity.slack;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MakersUserSlackRepository extends JpaRepository<MakersUserSlack, Long> {

	@Query("select m from makers_user_slack m where m.callEmoji = :emoji")
	Optional<MakersUserSlack> findByUserName(@Param("emoji") String emoji);
}