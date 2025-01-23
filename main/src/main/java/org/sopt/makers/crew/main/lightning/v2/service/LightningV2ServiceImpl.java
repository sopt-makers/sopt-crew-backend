package org.sopt.makers.crew.main.lightning.v2.service;

import static org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus.*;
import static org.sopt.makers.crew.main.global.constant.CrewConst.*;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.List;

import org.sopt.makers.crew.main.entity.apply.Applies;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.lightning.Lightning;
import org.sopt.makers.crew.main.entity.lightning.LightningRepository;
import org.sopt.makers.crew.main.entity.tag.enums.WelcomeMessageType;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserReader;
import org.sopt.makers.crew.main.global.dto.MeetingCreatorDto;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.lightning.v2.dto.mapper.LightningMapper;
import org.sopt.makers.crew.main.lightning.v2.dto.request.LightningV2CreateLightningBodyDto;
import org.sopt.makers.crew.main.lightning.v2.dto.response.LightningV2CreateLightningResponseDto;
import org.sopt.makers.crew.main.lightning.v2.dto.response.LightningV2GetLightningByMeetingIdResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.ApplyWholeInfoDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2CreateMeetingForLightningResponseDto;
import org.sopt.makers.crew.main.meeting.v2.service.MeetingV2Service;
import org.sopt.makers.crew.main.tag.v2.service.TagV2Service;
import org.sopt.makers.crew.main.user.v2.service.UserV2Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LightningV2ServiceImpl implements LightningV2Service {

	private static final int INTRO_IMAGE_LIST_SIZE = 1;

	private final UserV2Service userV2Service;
	private final TagV2Service tagV2Service;
	private final MeetingV2Service meetingV2Service;

	private final LightningRepository lightningRepository;
	private final ApplyRepository applyRepository;

	private final UserReader userReader;
	private final LightningMapper lightningMapper;

	private final Time realTime;

	@Override
	@Transactional
	public LightningV2CreateLightningResponseDto createLightning(
		LightningV2CreateLightningBodyDto requestBody, Integer userId) {
		User user = userV2Service.getUserByUserId(userId);

		if (user.getActivities() == null) {
			throw new BadRequestException(VALIDATION_EXCEPTION.getErrorCode());
		}

		if (requestBody.lightningBody().files().size() > INTRO_IMAGE_LIST_SIZE) {
			throw new BadRequestException(VALIDATION_EXCEPTION.getErrorCode());
		}

		MeetingV2CreateMeetingForLightningResponseDto meetingV2CreateMeetingForLightningResponseDto = meetingV2Service.createMeetingForLightning(
			userId, requestBody.lightningBody());

		Lightning lightning = lightningMapper.toLightningEntity(meetingV2CreateMeetingForLightningResponseDto,
			ACTIVE_GENERATION, user.getId(), realTime);

		lightningRepository.save(lightning);
		tagV2Service.createLightningTag(requestBody.welcomeMessageTypes(), lightning.getId());

		return LightningV2CreateLightningResponseDto.from(lightning.getMeetingId());
	}

	public LightningV2GetLightningByMeetingIdResponseDto getLightningByMeetingId(Integer meetingId, Integer userId) {
		User user = userV2Service.getUserByUserId(userId);

		Lightning lightning = lightningRepository.findByMeetingId(meetingId)
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_LIGHTNING.getErrorCode()));

		MeetingCreatorDto meetingLeader = userReader.getMeetingLeader(lightning.getLeaderUserId());

		Applies applies = new Applies(
			applyRepository.findAllByMeetingIdWithUser(meetingId, List.of(WAITING, APPROVE, REJECT), ORDER_ASC));

		boolean isHost = lightning.checkLightningMeetingLeader(user.getId());
		boolean isApply = applies.isApply(meetingId, user.getId());
		boolean isApproved = applies.isApproved(meetingId, user.getId());
		long approvedCount = applies.getApprovedCount(meetingId);

		List<ApplyWholeInfoDto> applyWholeInfoDtos = getApplyWholeInfoDtos(applies, meetingId, userId);

		List<WelcomeMessageType> welcomeMessageTypes = tagV2Service.getWelcomeMessageTypesByLightningId(
			lightning.getId());

		return LightningV2GetLightningByMeetingIdResponseDto.of(meetingId, lightning, welcomeMessageTypes,
			approvedCount, isHost, isApply, isApproved,
			meetingLeader, applyWholeInfoDtos, realTime.now());
	}

	private List<ApplyWholeInfoDto> getApplyWholeInfoDtos(Applies applies, Integer meetingId, Integer userId) {
		if (!applies.hasApplies(meetingId)) {
			return List.of();
		}

		return applies.getAppliesMap().get(meetingId).stream()
			.map(apply -> ApplyWholeInfoDto.of(apply, apply.getUser(), userId))
			.toList();
	}
}
