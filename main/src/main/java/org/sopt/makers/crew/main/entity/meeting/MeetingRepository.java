package org.sopt.makers.crew.main.entity.meeting;

import static org.sopt.makers.crew.main.common.response.ErrorStatus.NOT_FOUND_MEETING;

import java.util.List;
import java.util.Optional;
import org.sopt.makers.crew.main.common.exception.BadRequestException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting, Integer> {

  List<Meeting> findAllByUserId(int userId);

  Optional<Meeting> findById(Integer meetingId);

  default Meeting findByIdOrThrow(Integer meetingId) {
    return findById(meetingId)
        .orElseThrow(() -> new BadRequestException(NOT_FOUND_MEETING.getErrorCode()));
  }
}