package org.sopt.makers.crew.main.meeting.v2.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.meeting.CoLeader;
import org.sopt.makers.crew.main.entity.meeting.CoLeaderRepository;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRelatedMeetingExtractor {

	private final ApplyRepository applyRepository;
	private final CoLeaderRepository coLeaderRepository;
	private final MeetingRepository meetingRepository;

	public List<Meeting> extractMeetingsByUserId(Integer userId) {
		List<Apply> allByUserIdAndStatus = applyRepository.findAllByUserIdAndStatus(userId, EnApplyStatus.APPROVE);
		List<CoLeader> allByUserIdCoLeaderMeetings = coLeaderRepository.findAllByUserIdWithMeeting(userId);
		List<Meeting> allByUserIdMeetings = meetingRepository.findAllByUserId(userId);

		return Stream.of(allByUserIdCoLeaderMeetings.stream()
				.filter(coLeader -> Objects.nonNull(coLeader.getMeeting()))
				.map(CoLeader::getMeeting),
			allByUserIdMeetings.stream(), allByUserIdAndStatus.stream().map(
				Apply::getMeeting))
			.flatMap(stream -> stream)
			.filter(Objects::nonNull)
			.filter(meeting -> Objects.nonNull(meeting.getId()))
			.collect(Collectors.toMap(Meeting::getId, Function.identity(), (existing, ignored) -> existing,
				LinkedHashMap::new))
			.values()
			.stream()
			.toList();
	}

	public List<Integer> extractMeetingIdsByUserId(Integer userId) {
		List<Apply> allByUserIdAndStatus = applyRepository.findAllByUserIdAndStatus(userId, EnApplyStatus.APPROVE);
		List<CoLeader> allByUserIdCoLeaderMeetings = coLeaderRepository.findAllByUserIdWithMeeting(userId);
		List<Meeting> allByUserIdMeetings = meetingRepository.findAllByUserId(userId);

		return Stream.of(allByUserIdCoLeaderMeetings.stream()
					.filter(coLeader -> Objects.nonNull(coLeader.getMeeting()))
					.map(coLeader -> coLeader.getMeeting().getId()),
				allByUserIdMeetings.stream().map(Meeting::getId), allByUserIdAndStatus.stream().map(
					Apply::getMeetingId))
			.flatMap(stream -> stream)
			.filter(Objects::nonNull)
			.distinct()
			.toList();
	}
}
