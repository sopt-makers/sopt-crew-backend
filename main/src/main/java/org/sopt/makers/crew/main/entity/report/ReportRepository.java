package org.sopt.makers.crew.main.entity.report;

import java.util.Optional;
import org.sopt.makers.crew.main.entity.comment.Comment;
import org.sopt.makers.crew.main.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Integer> {

  Optional<Report> findByCommentAndUser(Comment comment, User user);
}
