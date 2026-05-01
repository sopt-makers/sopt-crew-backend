package org.sopt.makers.crew.main.advertisement.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.sopt.makers.crew.main.advertisement.dto.AdvertisementMeetingTopGetResponseDto;
import org.sopt.makers.crew.main.advertisement.dto.AdvertisementsGetResponseDto;
import org.sopt.makers.crew.main.advertisement.dto.AdvertisementsGetResponseDto.AdvertisementGetDto;
import org.sopt.makers.crew.main.entity.advertisement.Advertisement;
import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2ParticipatingPartInfoDto;
import org.sopt.makers.crew.main.meeting.v2.service.MeetingPartNormalizer;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdvertisementFactory {

	private static final String SOPKATHON_APPLY_TITLE_FORMAT = "[%d기 솝커톤] %s 파트 신청";
	private static final String SOPKATHON_BROWSE_QUERY_FORMAT = "%d기 솝커톤";

	private final MeetingPartNormalizer meetingPartNormalizer;

	public AdvertisementsGetResponseDto createAdvertisementsResponse(List<Advertisement> advertisements) {
		List<AdvertisementGetDto> advertisementDtos = advertisements.stream()
			.map(AdvertisementGetDto::of)
			.toList();
		return AdvertisementsGetResponseDto.of(advertisementDtos);
	}

	public String createSopkathonApplyTitle(Integer generation, MeetingJoinablePart part) {
		return String.format(SOPKATHON_APPLY_TITLE_FORMAT, generation, part.getDisplayName());
	}

	public String createSopkathonBrowseQuery(Integer generation) {
		return String.format(SOPKATHON_BROWSE_QUERY_FORMAT, generation);
	}

	public MeetingV2ParticipatingPartInfoDto createParticipatingPartInfo(UserActivityVO requestUserActivity,
		List<Apply> participatingApplies) {
		Optional<MeetingJoinablePart> requestUserPart = meetingPartNormalizer.findJoinablePart(
			requestUserActivity.getPart());
		if (requestUserPart.isEmpty()) {
			return MeetingV2ParticipatingPartInfoDto.of(requestUserActivity.getPart(), 0);
		}

		Map<Integer, Optional<MeetingJoinablePart>> recentJoinablePartByUserId = createRecentJoinablePartByUserId(
			participatingApplies);

		int participantCount = (int)participatingApplies.stream()
			.filter(apply -> isSamePartParticipatingApply(apply, requestUserPart.get(), recentJoinablePartByUserId))
			.count();

		return MeetingV2ParticipatingPartInfoDto.of(requestUserActivity.getPart(), participantCount);
	}

	private Map<Integer, Optional<MeetingJoinablePart>> createRecentJoinablePartByUserId(List<Apply> participatingApplies) {
		return participatingApplies.stream()
			.collect(Collectors.toMap(
				Apply::getUserId,
				apply -> meetingPartNormalizer.findJoinablePart(
					apply.getUser().getRecentActivityVO().getPart()),
				(existing, replacement) -> existing
			));
	}

	private boolean isSamePartParticipatingApply(Apply apply, MeetingJoinablePart requestUserPart,
		Map<Integer, Optional<MeetingJoinablePart>> recentJoinablePartByUserId) {
		return recentJoinablePartByUserId.getOrDefault(apply.getUserId(), Optional.empty())
			.filter(requestUserPart::equals)
			.isPresent();
	}
}
