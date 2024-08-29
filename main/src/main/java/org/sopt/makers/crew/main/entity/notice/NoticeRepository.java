package org.sopt.makers.crew.main.entity.notice;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Integer> {

	List<Notice> findByExposeStartDateBeforeAndExposeEndDateAfter(LocalDateTime now1, LocalDateTime now2);
}
