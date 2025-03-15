package org.sopt.makers.crew.main.flash.v2.service;

import static org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus.*;
import static org.sopt.makers.crew.main.global.constant.CrewConst.*;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.sopt.makers.crew.main.entity.apply.Applies;
import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.flash.Flash;
import org.sopt.makers.crew.main.entity.flash.FlashRepository;
import org.sopt.makers.crew.main.entity.tag.enums.WelcomeMessageType;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserReader;
import org.sopt.makers.crew.main.external.notification.dto.event.FlashCreatedEventDto;
import org.sopt.makers.crew.main.flash.v2.dto.mapper.FlashMapper;
import org.sopt.makers.crew.main.flash.v2.dto.request.FlashV2CreateAndUpdateFlashBodyDto;
import org.sopt.makers.crew.main.flash.v2.dto.response.FlashV2CreateAndUpdateResponseDto;
import org.sopt.makers.crew.main.flash.v2.dto.response.FlashV2GetFlashByMeetingIdResponseDto;
import org.sopt.makers.crew.main.global.dto.MeetingCreatorDto;
import org.sopt.makers.crew.main.global.dto.OrgIdListDto;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.sopt.makers.crew.main.global.util.ActiveGenerationProvider;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.meeting.v2.dto.response.ApplyWholeInfoDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2CreateAndUpdateMeetingForFlashResponseDto;
import org.sopt.makers.crew.main.meeting.v2.service.MeetingV2Service;
import org.sopt.makers.crew.main.tag.v2.service.TagV2Service;
import org.sopt.makers.crew.main.user.v2.service.UserV2Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlashV2ServiceImpl implements FlashV2Service {

	private static final int INTRO_IMAGE_LIST_SIZE = 1;

	private final UserV2Service userV2Service;
	private final TagV2Service tagV2Service;
	private final MeetingV2Service meetingV2Service;

	private final FlashRepository flashRepository;
	private final ApplyRepository applyRepository;

	private final UserReader userReader;
	private final FlashMapper flashMapper;

	private final ApplicationEventPublisher eventPublisher;
	private final ActiveGenerationProvider activeGenerationProvider;
	private final Time realTime;

	@Override
	@Transactional
	public FlashV2CreateAndUpdateResponseDto createFlash(
		FlashV2CreateAndUpdateFlashBodyDto requestBody, Integer userId) {
		User user = userV2Service.getUserByUserId(userId);

		if (user.getActivities() == null) {
			throw new BadRequestException(VALIDATION_EXCEPTION.getErrorCode());
		}

		if (requestBody.flashBody().files().size() > INTRO_IMAGE_LIST_SIZE) {
			throw new BadRequestException(VALIDATION_EXCEPTION.getErrorCode());
		}

		MeetingV2CreateAndUpdateMeetingForFlashResponseDto meetingV2CreateAndUpdateMeetingForFlashResponseDto = meetingV2Service.createMeetingForFlash(
			userId, requestBody.flashBody());

		Flash flash = flashMapper.toFlashEntity(meetingV2CreateAndUpdateMeetingForFlashResponseDto,
			activeGenerationProvider.getActiveGeneration(), user.getId(), realTime);

		flashRepository.save(flash);
		tagV2Service.createFlashTag(requestBody.welcomeMessageTypes(), flash.getId());

		OrgIdListDto orgIdListDto = userReader.findAllOrgIds();

		eventPublisher.publishEvent(
			new FlashCreatedEventDto(orgIdListDto.getOrgIds(), flash.getMeetingId(), flash.getTitle()));

		return FlashV2CreateAndUpdateResponseDto.from(flash.getMeetingId());
	}

	public FlashV2GetFlashByMeetingIdResponseDto getFlashByMeetingId(Integer meetingId, Integer userId) {
		User user = userV2Service.getUserByUserId(userId);

		Flash flash = flashRepository.findByMeetingId(meetingId)
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_FLASH.getErrorCode()));

		MeetingCreatorDto meetingLeader = userReader.getMeetingLeader(flash.getLeaderUserId());

		Applies applies = new Applies(
			applyRepository.findAllByMeetingIdWithUser(meetingId, List.of(WAITING, APPROVE, REJECT), ORDER_ASC));

		boolean isHost = flash.checkFlashMeetingLeader(user.getId());
		boolean isApply = applies.isApply(meetingId, user.getId());
		boolean isApproved = applies.isApproved(meetingId, user.getId());
		long approvedCount = applies.getApprovedCount(meetingId);

		List<ApplyWholeInfoDto> applyWholeInfoDtos = getApplyWholeInfoDtos(applies, meetingId, userId);

		List<WelcomeMessageType> welcomeMessageTypes = tagV2Service.getWelcomeMessageTypesByFlashId(
			flash.getId());

		return FlashV2GetFlashByMeetingIdResponseDto.of(meetingId, flash, welcomeMessageTypes,
			approvedCount, isHost, isApply, isApproved,
			meetingLeader, applyWholeInfoDtos, realTime.now());
	}

	@Caching(evict = {
		@CacheEvict(value = "meetingCache", key = "#meetingId"),
		@CacheEvict(value = "meetingLeaderCache", key = "#userId"),
	})
	@Override
	@Transactional
	public FlashV2CreateAndUpdateResponseDto updateFlash(Integer meetingId,
		FlashV2CreateAndUpdateFlashBodyDto requestBody,
		Integer userId) {
		User user = userV2Service.getUserByUserId(userId);

		Flash flash = flashRepository.findByMeetingId(meetingId)
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_FLASH.getErrorCode()));

		if (user.getActivities() == null) {
			throw new BadRequestException(VALIDATION_EXCEPTION.getErrorCode());
		}

		if (requestBody.flashBody().files().size() > INTRO_IMAGE_LIST_SIZE) {
			throw new BadRequestException(VALIDATION_EXCEPTION.getErrorCode());
		}

		MeetingV2CreateAndUpdateMeetingForFlashResponseDto meetingV2CreateAndUpdateMeetingForFlashResponseDto = meetingV2Service.updateMeetingForFlash(
			meetingId, userId, requestBody.flashBody());

		Flash updatedFlash = flashMapper.toFlashEntity(meetingV2CreateAndUpdateMeetingForFlashResponseDto,
			activeGenerationProvider.getActiveGeneration(), user.getId(), realTime);

		flash.updateFlash(updatedFlash);

		tagV2Service.updateFlashTag(requestBody.welcomeMessageTypes(), flash.getId());

		return FlashV2CreateAndUpdateResponseDto.from(flash.getMeetingId());
	}

	private List<ApplyWholeInfoDto> getApplyWholeInfoDtos(Applies applies, Integer meetingId, Integer userId) {
		if (!applies.hasApplies(meetingId)) {
			return List.of();
		}
		AtomicInteger applyNumber = new AtomicInteger(1);

		return applies.getAppliesMap().get(meetingId).stream()
			.sorted(Comparator.comparing(Apply::getAppliedDate))
			.map(apply -> ApplyWholeInfoDto.ofIncludeApplyNumber(apply, apply.getUser(), userId,
				applyNumber.getAndIncrement()))
			.toList();
	}
}
