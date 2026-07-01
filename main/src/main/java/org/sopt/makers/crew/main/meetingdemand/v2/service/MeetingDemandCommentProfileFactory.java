package org.sopt.makers.crew.main.meetingdemand.v2.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandComment;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandCommentProfile;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandCommentProfileRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MeetingDemandCommentProfileFactory {

	private final MeetingDemandCommentProfileRepository meetingDemandCommentProfileRepository;

	public MeetingDemandCommentProfile findOrCreate(Integer meetingDemandId, Integer userId) {
		return meetingDemandCommentProfileRepository.findByMeetingDemandIdAndUserId(meetingDemandId, userId)
			.orElseGet(() -> meetingDemandCommentProfileRepository.save(MeetingDemandCommentProfile.builder()
				.meetingDemandId(meetingDemandId)
				.userId(userId)
				.build()));
	}

	public Map<Integer, MeetingDemandCommentProfile> createProfileMap(Integer meetingDemandId,
		List<MeetingDemandComment> comments) {
		Set<Integer> userIds = comments.stream()
			.map(MeetingDemandComment::getUserId)
			.filter(userId -> userId != null)
			.collect(Collectors.toSet());
		if (userIds.isEmpty()) {
			return Map.of();
		}

		return meetingDemandCommentProfileRepository.findAllByMeetingDemandIdAndUserIdIn(meetingDemandId, userIds)
			.stream()
			.collect(Collectors.toMap(MeetingDemandCommentProfile::getUserId, Function.identity()));
	}
}
