package org.sopt.makers.crew.main.advertisement.service;

import static org.sopt.makers.crew.main.entity.advertisement.enums.AdvertisementCategory.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.sopt.makers.crew.main.admin.v2.dto.AdminMeetingTopAdvertisementResponse;
import org.sopt.makers.crew.main.admin.v2.dto.AdvertisementMeetingTopUpdateRequest;
import org.sopt.makers.crew.main.advertisement.dto.AdvertisementMeetingTopGetResponseDto;
import org.sopt.makers.crew.main.advertisement.dto.AdvertisementsGetResponseDto;
import org.sopt.makers.crew.main.entity.advertisement.Advertisement;
import org.sopt.makers.crew.main.entity.advertisement.AdvertisementRepository;
import org.sopt.makers.crew.main.entity.advertisement.enums.AdvertisementCategory;
import org.sopt.makers.crew.main.entity.advertisement.enums.EventType;
import org.sopt.makers.crew.main.entity.advertisement.enums.TargetGeneration;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.global.util.ActiveGenerationProvider;
import org.sopt.makers.crew.main.global.util.Time;
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

	private final AdvertisementRepository advertisementRepository;
	private final MeetingRepository meetingRepository;
	private final UserRepository userRepository;
	private final ActiveGenerationProvider activeGenerationProvider;
	private final MeetingPartNormalizer meetingPartNormalizer;
	private final Time time;
	private final AdvertisementValidator advertisementValidator;
	private final AdvertisementFactory advertisementFactory;

	public AdvertisementsGetResponseDto getAdvertisement(AdvertisementCategory advertisementCategory) {
		advertisementValidator.validateGeneralAdvertisementCategory(advertisementCategory);

		LocalDateTime now = time.now();

		int maxItems = advertisementCategory.getMaxItems();
		Pageable pageable = PageRequest.of(0, maxItems);

		List<Advertisement> advertisements = advertisementRepository.findAdvertisementsByDateAndType(
			advertisementCategory,
			true,
			now,
			pageable);

		if (!advertisements.isEmpty()) {
			return advertisementFactory.createAdvertisementsResponse(advertisements);
		}

		advertisements = advertisementRepository.findAdvertisementsByCategory(
			advertisementCategory,
			false,
			pageable);

		return advertisementFactory.createAdvertisementsResponse(advertisements);
	}

	public AdvertisementMeetingTopGetResponseDto getMeetingTopAdvertisement(Integer userId, EventType eventType) {
		if (eventType == null) {
			return AdvertisementMeetingTopGetResponseDto.notDisplay();
		}

		LocalDateTime now = time.now();

		List<Advertisement> advertisements = advertisementRepository.findMeetingTopAdvertisements(MEETING_TOP, now);

		return advertisements.stream()
			.filter(advertisement -> advertisement.getEventType() == eventType)
			.map(advertisement -> createMeetingTopResponse(advertisement, userId))
			.flatMap(Optional::stream)
			.findFirst()
			.orElseGet(AdvertisementMeetingTopGetResponseDto::notDisplay);
	}

	@Transactional
	public Advertisement updateMeetingTopAdvertisementDisplay(Integer advertisementId, boolean isDisplay) {
		return updateMeetingTopAdvertisement(
			advertisementId,
			AdvertisementMeetingTopUpdateRequest.display(isDisplay)
		);
	}

	@Transactional
	public Advertisement updateMeetingTopAdvertisement(Integer advertisementId,
		AdvertisementMeetingTopUpdateRequest request) {
		Advertisement advertisement = advertisementRepository.findByIdOrThrow(advertisementId);

		advertisementValidator.validateMeetingTopAdvertisement(advertisement);
		advertisementValidator.validateMeetingTopUpdateRequest(advertisement, request);

		Boolean isDisplay = request.isDisplay();
		if (isDisplay != null) {
			advertisementValidator.validateSingleMeetingTopDisplay(advertisement, isDisplay);
		}

		advertisement.updateMeetingTopAdvertisement(
			request.isDisplay(),
			request.advertisementStartDate(),
			request.advertisementEndDate(),
			request.desktopImageUrl(),
			request.mobileImageUrl(),
			request.calendarImageUrl(),
			request.titlePrefix(),
			request.titleHighlight(),
			request.titleSuffix(),
			request.subTitle()
		);

		return advertisement;
	}

	public List<AdminMeetingTopAdvertisementResponse> getMeetingTopAdvertisementsForAdmin() {
		return advertisementRepository.findAdvertisementsByCategoryForAdmin(MEETING_TOP).stream()
			.map(AdminMeetingTopAdvertisementResponse::from)
			.toList();
	}

	private Optional<AdvertisementMeetingTopGetResponseDto> createMeetingTopResponse(Advertisement advertisement,
		Integer userId) {
		if (advertisement.getEventType() == null) {
			return Optional.empty();
		}

		return switch (advertisement.getEventType()) {
			case SOPKATHON -> createSopkathonMeetingTopResponse(advertisement, userId);
			case NETWORKING -> Optional.of(createNetworkingMeetingTopResponse(advertisement));
		};
	}

	private Optional<AdvertisementMeetingTopGetResponseDto> createSopkathonMeetingTopResponse(Advertisement advertisement,
		Integer userId) {
		User user = userRepository.findByIdOrThrow(userId);
		Optional<UserActivityVO> targetActivity = findTargetActivity(user, advertisement.getTargetGeneration());
		if (targetActivity.isEmpty()) {
			return Optional.empty();
		}

		Optional<MeetingJoinablePart> meetingJoinablePart = meetingPartNormalizer.findJoinablePart(
			targetActivity.get().getPart());
		if (meetingJoinablePart.isEmpty()) {
			return Optional.empty();
		}

		Integer activeGeneration = activeGenerationProvider.getActiveGeneration();
		Integer bannerLink2MeetingId = findSopkathonApplicationMeetingId(
			activeGeneration,
			meetingJoinablePart.get()
		).orElse(null);

		return Optional.of(AdvertisementMeetingTopGetResponseDto.of(
			advertisement,
			activeGeneration,
			bannerLink2MeetingId
		));
	}

	private AdvertisementMeetingTopGetResponseDto createNetworkingMeetingTopResponse(Advertisement advertisement) {
		Integer activeGeneration = activeGenerationProvider.getActiveGeneration();
		Integer bannerLink2MeetingId = findNetworkingApplicationMeetingId(activeGeneration).orElse(null);

		return AdvertisementMeetingTopGetResponseDto.ofNetworking(
			advertisement,
			bannerLink2MeetingId
		);
	}

	private Optional<Integer> findSopkathonApplicationMeetingId(Integer activeGeneration, MeetingJoinablePart part) {
		String title = advertisementFactory.createSopkathonApplyTitle(activeGeneration, part);
		return meetingRepository.findFirstByTitleOrderByIdDesc(title)
			.map(Meeting::getId);
	}

	private Optional<Integer> findNetworkingApplicationMeetingId(Integer activeGeneration) {
		String titleQuery = advertisementFactory.createNetworkingTitleQuery(activeGeneration);
		return meetingRepository.findFirstByTitleContainingOrderByIdDesc(titleQuery)
			.map(Meeting::getId);
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
}
