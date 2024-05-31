package org.sopt.makers.crew.main.entity.apply;

import static org.sopt.makers.crew.main.common.response.ErrorStatus.NOT_FOUND_APPLY;

import java.util.List;
import java.util.Optional;
import org.sopt.makers.crew.main.common.exception.BadRequestException;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ApplyRepository extends JpaRepository<Apply, Integer>, ApplySearchRepository {

    @Query("select a from Apply a join fetch a.meeting m where a.userId = :userId and a.status = :statusValue")
    List<Apply> findAllByUserIdAndStatus(@Param("userId") Integer userId,
                                         @Param("statusValue") EnApplyStatus statusValue);

    List<Apply> findAllByMeetingIdAndStatus(Integer meetingId, EnApplyStatus statusValue);

    boolean existsByMeetingIdAndUserId(Integer meetingId, Integer userId);

    @Transactional
    @Modifying
    @Query("delete from Apply a where a.meeting.id = :meetingId and a.userId = :userId")
    void deleteByMeetingIdAndUserId(@Param("meetingId") Integer meetingId, @Param("userId") Integer userId);

    Optional<Apply> findById(Integer applyId);

    default Apply findByIdOrThrow(Integer applyId) {
        return findById(applyId)
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_APPLY.getErrorCode()));
    }

}