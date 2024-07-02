package org.sopt.makers.crew.main.entity.meeting;

import java.util.List;
import org.sopt.makers.crew.main.entity.apply.Apply;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingQueryRepository {

    List<Meeting> findCreatedMeetingByUser(int userId);

    List<Apply> findAppliedMeetingByUser(int userId);

}
