package org.sopt.makers.crew.main.entity.meeting;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting, Integer> {

  List<Meeting> findAllByUserId(int userId);
}
