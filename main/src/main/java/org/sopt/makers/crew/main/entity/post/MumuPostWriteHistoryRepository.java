package org.sopt.makers.crew.main.entity.post;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MumuPostWriteHistoryRepository extends JpaRepository<MumuPostWriteHistory, Integer> {

	boolean existsByUserIdAndWrittenDate(Integer userId, LocalDate writtenDate);
}
