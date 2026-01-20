package org.sopt.makers.crew.main.meeting.v2.dto;

import java.util.List;

import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.meeting.CoLeader;
import org.sopt.makers.crew.main.entity.meeting.CoLeaders;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.user.User;

public record ApplyDataContext(
	Meeting meeting,
	User user,
	List<CoLeader> coLeaderList,
	List<Apply> applies
) {
	public CoLeaders getCoLeaders() {
		return new CoLeaders(coLeaderList);
	}
}
