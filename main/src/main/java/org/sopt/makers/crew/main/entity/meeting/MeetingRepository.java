package org.sopt.makers.crew.main.entity.meeting;

import java.util.List;
import java.util.Optional;
import org.sopt.makers.crew.main.common.exception.BadRequestException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting, Integer> {

  List<Meeting> findAllByUserId(int userId);

  Optional<Meeting> findById(Integer meetingId);

  default Meeting findByIdOrThrow(Integer meetingId) {
    return findById(meetingId)
        .orElseThrow(() -> new BadRequestException("모임이 없습니다."));
  }
}
