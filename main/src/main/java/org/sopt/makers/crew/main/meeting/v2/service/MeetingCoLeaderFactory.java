package org.sopt.makers.crew.main.meeting.v2.service;

import java.util.List;

import org.sopt.makers.crew.main.entity.meeting.CoLeader;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.user.User;
import org.springframework.stereotype.Component;

@Component
public class MeetingCoLeaderFactory {

	public List<CoLeader> createCoLeaders(List<User> coLeaders, Meeting meeting) {
		return coLeaders.stream()
			.map(coLeader -> CoLeader.builder()
				.meeting(meeting)
				.user(coLeader)
				.build())
			.toList();
	}
}
