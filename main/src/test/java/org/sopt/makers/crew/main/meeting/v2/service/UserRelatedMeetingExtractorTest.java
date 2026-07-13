package org.sopt.makers.crew.main.meeting.v2.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.meeting.CoLeader;
import org.sopt.makers.crew.main.entity.meeting.CoLeaderRepository;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;

@ExtendWith(MockitoExtension.class)
class UserRelatedMeetingExtractorTest {

	@InjectMocks
	private UserRelatedMeetingExtractor userRelatedMeetingExtractor;

	@Mock
	private ApplyRepository applyRepository;
	@Mock
	private CoLeaderRepository coLeaderRepository;
	@Mock
	private MeetingRepository meetingRepository;

	@Test
	void extractMeetingIdsByUserIdReturnsDistinctIdsFromAppliesCoLeadersAndCreatedMeetings() {
		Integer userId = 1;
		Apply apply = apply(1);
		Apply duplicatedApply = apply(2);
		Apply nullApply = apply((Integer)null);
		CoLeader coLeader = coLeader(meeting(2));
		CoLeader anotherCoLeader = coLeader(meeting(3));
		CoLeader nullMeetingCoLeader = coLeader(null);
		Meeting createdMeeting = meeting(4);
		Meeting duplicatedCreatedMeeting = meeting(1);

		when(applyRepository.findAllByUserIdAndStatus(userId, EnApplyStatus.APPROVE))
			.thenReturn(List.of(apply, duplicatedApply, nullApply));
		when(coLeaderRepository.findAllByUserIdWithMeeting(userId))
			.thenReturn(List.of(coLeader, anotherCoLeader, nullMeetingCoLeader));
		when(meetingRepository.findAllByUserId(userId))
			.thenReturn(List.of(createdMeeting, duplicatedCreatedMeeting));

		List<Integer> meetingIds = userRelatedMeetingExtractor.extractMeetingIdsByUserId(userId);

		assertThat(meetingIds).containsExactlyInAnyOrder(1, 2, 3, 4);
	}

	@Test
	void extractMeetingsByUserIdReturnsDistinctMeetingsFromAppliesCoLeadersAndCreatedMeetings() {
		Integer userId = 1;
		Meeting appliedMeeting = meeting(1);
		Meeting duplicatedAppliedMeeting = meeting(2);
		Meeting coLeaderMeeting = meeting(2);
		Meeting anotherCoLeaderMeeting = meeting(3);
		Meeting createdMeeting = meeting(4);
		Meeting duplicatedCreatedMeeting = meeting(1);
		Apply apply = apply(appliedMeeting);
		Apply duplicatedApply = apply(duplicatedAppliedMeeting);
		Apply nullApply = apply((Meeting)null);
		CoLeader coLeader = coLeader(coLeaderMeeting);
		CoLeader anotherCoLeader = coLeader(anotherCoLeaderMeeting);
		CoLeader nullMeetingCoLeader = coLeader(null);

		when(applyRepository.findAllByUserIdAndStatus(userId, EnApplyStatus.APPROVE))
			.thenReturn(List.of(apply, duplicatedApply, nullApply));
		when(coLeaderRepository.findAllByUserIdWithMeeting(userId))
			.thenReturn(List.of(coLeader, anotherCoLeader, nullMeetingCoLeader));
		when(meetingRepository.findAllByUserId(userId))
			.thenReturn(List.of(createdMeeting, duplicatedCreatedMeeting));

		List<Meeting> meetings = userRelatedMeetingExtractor.extractMeetingsByUserId(userId);

		assertThat(meetings)
			.extracting(Meeting::getId)
			.containsExactly(2, 3, 4, 1);
	}

	@Test
	void extractMeetingIdsByUserIdReturnsEmptyWhenUserHasNoRelatedMeetings() {
		Integer userId = 1;
		when(applyRepository.findAllByUserIdAndStatus(userId, EnApplyStatus.APPROVE)).thenReturn(List.of());
		when(coLeaderRepository.findAllByUserIdWithMeeting(userId)).thenReturn(List.of());
		when(meetingRepository.findAllByUserId(userId)).thenReturn(List.of());

		List<Integer> meetingIds = userRelatedMeetingExtractor.extractMeetingIdsByUserId(userId);

		assertThat(meetingIds).isEmpty();
	}

	private Apply apply(Integer meetingId) {
		Apply apply = mock(Apply.class);
		when(apply.getMeetingId()).thenReturn(meetingId);
		return apply;
	}

	private Apply apply(Meeting meeting) {
		Apply apply = mock(Apply.class);
		when(apply.getMeeting()).thenReturn(meeting);
		return apply;
	}

	private CoLeader coLeader(Meeting meeting) {
		CoLeader coLeader = mock(CoLeader.class);
		when(coLeader.getMeeting()).thenReturn(meeting);
		return coLeader;
	}

	private Meeting meeting(Integer id) {
		Meeting meeting = mock(Meeting.class);
		when(meeting.getId()).thenReturn(id);
		return meeting;
	}
}
