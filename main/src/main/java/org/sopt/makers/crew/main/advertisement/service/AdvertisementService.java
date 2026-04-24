package org.sopt.makers.crew.main.advertisement.service;

import static org.sopt.makers.crew.main.entity.advertisement.enums.AdvertisementCategory.*;
import static org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus.*;
import static org.sopt.makers.crew.main.entity.meeting.enums.EnMeetingStatus.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.sopt.makers.crew.main.advertisement.dto.AdvertisementMeetingTopGetResponseDto;
import org.sopt.makers.crew.main.advertisement.dto.AdvertisementsGetResponseDto;
import org.sopt.makers.crew.main.advertisement.dto.AdvertisementsGetResponseDto.AdvertisementGetDto;
import org.sopt.makers.crew.main.entity.advertisement.Advertisement;
import org.sopt.makers.crew.main.entity.advertisement.AdvertisementRepository;
import org.sopt.makers.crew.main.entity.advertisement.enums.AdvertisementCategory;
import org.sopt.makers.crew.main.entity.advertisement.enums.EventType;
import org.sopt.makers.crew.main.entity.advertisement.enums.TargetGeneration;
import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.util.ActiveGenerationProvider;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2ParticipatingPartInfoDto;
import org.sopt.makers.crew.main.meeting.v2.service.MeetingPartNormalizer;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdvertisementService {

	private static final String ORDER_ASC = "ASC";
	private static final String SOPKATHON_APPLY_TITLE_FORMAT = "[%d기 솝커톤] %s 파트 신청";
	private static final String SOPKATHON_BROWSE_QUERY_FORMAT = "%d기 솝커톤";

	private final AdvertisementRepository advertisementRepository;
	private final MeetingRepository meetingRepository;
	private final UserRepository userRepository;
	private final ApplyRepository applyRepository;
	private final ActiveGenerationProvider activeGenerationProvider;
	private final MeetingPartNormalizer meetingPartNormalizer;
	private final Time time;

	public AdvertisementsGetResponseDto getAdvertisement(AdvertisementCategory advertisementCategory) {
		validateGeneralAdvertisementCategory(advertisementCategory);

		LocalDateTime now = time.now();

		int maxItems = advertisementCategory.getMaxItems();
		Pageable pageable = PageRequest.of(0, maxItems);

		List<Advertisement> advertisements = advertisementRepository.findAdvertisementsByDateAndType(
			advertisementCategory,
			true,
			now,
			pageable);

		if (!advertisements.isEmpty()) {
			return createResponseDto(advertisements);
		}

		advertisements = advertisementRepository.findAdvertisementsByCategory(
			advertisementCategory,
			false,
			pageable);

		return createResponseDto(advertisements);
	}

	public AdvertisementMeetingTopGetResponseDto getMeetingTopAdvertisement(Integer userId) {
		LocalDateTime now = time.now();
		User user = userRepository.findByIdOrThrow(userId);

		List<Advertisement> advertisements = advertisementRepository.findMeetingTopAdvertisements(MEETING_TOP, now);

		return advertisements.stream()
			.map(advertisement -> createMeetingTopResponse(advertisement, user, now))
			.flatMap(Optional::stream)
			.findFirst()
			.orElseGet(AdvertisementMeetingTopGetResponseDto::notDisplay);
	}

	@Transactional
	public Advertisement updateMeetingTopAdvertisementDisplay(Integer advertisementId, boolean isDisplay) {
		Advertisement advertisement = advertisementRepository.findByIdOrThrow(advertisementId);

		validateMeetingTopAdvertisement(advertisement);
		validateSingleMeetingTopDisplay(advertisement, isDisplay);

		advertisement.updateDisplay(isDisplay);

		return advertisement;
	}

	private void validateGeneralAdvertisementCategory(AdvertisementCategory advertisementCategory) {
		if (!advertisementCategory.isGeneralAdvertisement()) {
			throw new BadRequestException("일반 광고 조회에서 허용하지 않는 카테고리입니다.");
		}
	}

	private void validateMeetingTopAdvertisement(Advertisement advertisement) {
		if (!MEETING_TOP.equals(advertisement.getAdvertisementCategory())) {
			throw new BadRequestException("모임 상단 광고만 노출 여부를 수정할 수 있습니다.");
		}
	}

	private void validateSingleMeetingTopDisplay(Advertisement advertisement, boolean isDisplay) {
		if (!shouldTurnOn(advertisement, isDisplay)) {
			return;
		}

		boolean existsOtherDisplayedAdvertisement = advertisementRepository.existsDisplayedAdvertisementByCategoryExcludingId(
			MEETING_TOP,
			advertisement.getId());
		if (existsOtherDisplayedAdvertisement) {
			throw new BadRequestException("모임 상단 광고는 하나만 노출할 수 있습니다.");
		}
	}

	private boolean shouldTurnOn(Advertisement advertisement, boolean isDisplay) {
		return isDisplay && !advertisement.isDisplay();
	}

	private AdvertisementsGetResponseDto createResponseDto(List<Advertisement> advertisements) {
		List<AdvertisementGetDto> advertisementDtos = advertisements.stream()
			.map(AdvertisementGetDto::of)
			.toList();
		return AdvertisementsGetResponseDto.of(advertisementDtos);
	}

	private Optional<AdvertisementMeetingTopGetResponseDto> createMeetingTopResponse(Advertisement advertisement,
		User user, LocalDateTime now) {
		if (!EventType.SOPKATHON.equals(advertisement.getEventType())) {
			return Optional.empty();
		}

		Optional<UserActivityVO> targetActivity = findTargetActivity(user, advertisement.getTargetGeneration());
		if (targetActivity.isEmpty()) {
			return Optional.empty();
		}

		Optional<MeetingJoinablePart> meetingJoinablePart = meetingPartNormalizer.findJoinablePart(
			targetActivity.get().getPart());
		if (meetingJoinablePart.isEmpty()) {
			return Optional.empty();
		}

		String applyTitle = createSopkathonApplyTitle(targetActivity.get().getGeneration(), meetingJoinablePart.get());
		Optional<Meeting> meeting = meetingRepository.findFirstByTitleOrderByIdDesc(applyTitle);
		if (meeting.isEmpty() || RECRUITMENT_COMPLETE.equals(meeting.get().getMeetingStatus(now))) {
			return Optional.empty();
		}

		List<Apply> participatingApplies = getParticipatingApplies(meeting.get().getId());
		MeetingV2ParticipatingPartInfoDto participatingPartInfo = createParticipatingPartInfo(targetActivity.get(),
			participatingApplies);
		String browseQuery = createSopkathonBrowseQuery(targetActivity.get().getGeneration());

		return Optional.of(AdvertisementMeetingTopGetResponseDto.of(advertisement, meeting.get(), targetActivity.get(),
			participatingPartInfo, browseQuery, now));
	}

	private Optional<UserActivityVO> findTargetActivity(User user, TargetGeneration targetGeneration) {
		TargetGeneration resolvedTargetGeneration = targetGeneration == null ? TargetGeneration.ALL : targetGeneration;
		return switch (resolvedTargetGeneration) {
			case ACTIVE -> findActiveGenerationActivity(user);
			case RECENT -> findRecentActivity(user);
			case ALL -> findActiveGenerationActivity(user).or(() -> findRecentActivity(user));
		};
	}

	private Optional<UserActivityVO> findActiveGenerationActivity(User user) {
		return getActivities(user).stream()
			.filter(activity -> activity.getGeneration() == activeGenerationProvider.getActiveGeneration())
			.filter(activity -> meetingPartNormalizer.findJoinablePart(activity.getPart()).isPresent())
			.findFirst();
	}

	private Optional<UserActivityVO> findRecentActivity(User user) {
		return getActivities(user).stream()
			.filter(activity -> meetingPartNormalizer.findJoinablePart(activity.getPart()).isPresent())
			.max(Comparator.comparingInt(UserActivityVO::getGeneration));
	}

	private List<UserActivityVO> getActivities(User user) {
		if (user.getActivities() == null) {
			return List.of();
		}
		return user.getActivities();
	}

	private String createSopkathonApplyTitle(Integer generation, MeetingJoinablePart part) {
		return String.format(SOPKATHON_APPLY_TITLE_FORMAT, generation, meetingPartNormalizer.getDisplayName(part));
	}

	private String createSopkathonBrowseQuery(Integer generation) {
		return String.format(SOPKATHON_BROWSE_QUERY_FORMAT, generation);
	}

	private List<Apply> getParticipatingApplies(Integer meetingId) {
		return applyRepository.findAllByMeetingIdWithUser(meetingId, List.of(WAITING, APPROVE), ORDER_ASC);
	}

	private MeetingV2ParticipatingPartInfoDto createParticipatingPartInfo(UserActivityVO requestUserActivity,
		List<Apply> participatingApplies) {
		Optional<MeetingJoinablePart> requestUserPart = meetingPartNormalizer.findJoinablePart(
			requestUserActivity.getPart());
		if (requestUserPart.isEmpty()) {
			return MeetingV2ParticipatingPartInfoDto.of(requestUserActivity.getPart(), 0);
		}

		int participantCount = (int)participatingApplies.stream()
			.filter(apply -> isSamePartParticipatingApply(apply, requestUserPart.get()))
			.count();

		return MeetingV2ParticipatingPartInfoDto.of(requestUserActivity.getPart(), participantCount);
	}

	private boolean isSamePartParticipatingApply(Apply apply, MeetingJoinablePart requestUserPart) {
		return findRecentActivity(apply.getUser())
			.flatMap(activity -> meetingPartNormalizer.findJoinablePart(activity.getPart()))
			.filter(requestUserPart::equals)
			.isPresent();
	}
}
