package org.sopt.makers.crew.main.entity.meeting;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class JointLeaders {
	private final List<JointLeader> jointLeaders;

	public boolean isJointLeader(Integer requestUserId) {
		return jointLeaders.stream()
			.anyMatch(jointLeader -> jointLeader.getUser().getId().equals(requestUserId));
	}
}
