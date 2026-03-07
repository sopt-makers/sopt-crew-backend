package org.sopt.makers.crew.main.meeting.v2.service;

import java.util.List;

import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.meeting.CoLeader;
import org.sopt.makers.crew.main.entity.meeting.CoLeaderRepository;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.meeting.v2.dto.ApplyDataContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplyReadService {

	private final MeetingRepository meetingRepository;
	private final UserRepository userRepository;
	private final CoLeaderRepository coLeaderRepository;
	private final ApplyRepository applyRepository;
	private final SpikeApplyProfiler spikeApplyProfiler;

	@Transactional(readOnly = true)
	public ApplyDataContext fetchDataForApply(Integer meetingId, Integer userId, String txMode, String gate) {
		Meeting meeting = spikeApplyProfiler.profile("crew.spike.apply.business.fetch.meeting", txMode, gate,
			() -> meetingRepository.findByIdOrThrow(meetingId));
		User user = spikeApplyProfiler.profile("crew.spike.apply.business.fetch.user", txMode, gate,
			() -> userRepository.findByIdOrThrow(userId));
		List<CoLeader> coLeaders = spikeApplyProfiler.profile("crew.spike.apply.business.fetch.coleaders", txMode,
			gate, () -> coLeaderRepository.findAllByMeetingId(meetingId));
		List<Apply> applies = spikeApplyProfiler.profile("crew.spike.apply.business.fetch.applies", txMode, gate,
			() -> applyRepository.findAllByMeetingId(meetingId));

		return new ApplyDataContext(meeting, user, coLeaders, applies);
	}
}
