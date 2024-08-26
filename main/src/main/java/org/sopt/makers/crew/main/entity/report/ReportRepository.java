package org.sopt.makers.crew.main.entity.report;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Integer> {

	boolean existsByCommentIdAndUserId(Integer commentId, Integer userId);

	boolean existsByPostIdAndUserId(Integer postId, Integer userId);
}
