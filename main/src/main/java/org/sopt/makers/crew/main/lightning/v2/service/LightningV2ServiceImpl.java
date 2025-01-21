package org.sopt.makers.crew.main.lightning.v2.service;

import static org.sopt.makers.crew.main.global.constant.CrewConst.*;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import org.sopt.makers.crew.main.entity.lightning.Lightning;
import org.sopt.makers.crew.main.entity.lightning.LightningRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.lightning.v2.dto.mapper.LightningMapper;
import org.sopt.makers.crew.main.lightning.v2.dto.request.LightningV2CreateLightningBodyDto;
import org.sopt.makers.crew.main.lightning.v2.dto.response.LightningV2CreateLightningResponseDto;
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

		return LightningV2CreateLightningResponseDto.from(lightning.getId());
	}

	// @Override
	// public LightningV2GetLightningByIdResponseDto getLightningById(Integer lightningId, Integer userId) {
	// 	User user = userV2Service.getUserByUserId(userId);
	//
	// 	Lightning lightning = lightningRepository.findById(lightningId)
	// 		.orElseThrow(() -> new NotFoundException(NOT_FOUND_LIGHTNING.getErrorCode()));
	//
	// 	lightning.
	//
	// 		Meeting meeting = meetingReader.getMeetingById(meetingId).toEntity();
	// 	MeetingCreatorDto meetingLeader = userReader.getMeetingLeader(meeting.getUserId());
	// 	CoLeaders coLeaders = coLeaderReader.getCoLeaders(meetingId).toEntity();
	//
	// 	Applies applies = new Applies(
	// 		applyRepository.findAllByMeetingIdWithUser(meetingId, List.of(WAITING, APPROVE, REJECT), ORDER_ASC));
	//
	// 	Boolean isHost = meeting.checkMeetingLeader(user.getId());
	// 	Boolean isApply = applies.isApply(meetingId, user.getId());
	// 	Boolean isApproved = applies.isApproved(meetingId, user.getId());
	// 	boolean isCoLeader = coLeaders.isCoLeader(meetingId, userId);
	// 	long approvedCount = applies.getApprovedCount(meetingId);
	//
	// 	List<ApplyWholeInfoDto> applyWholeInfoDtos = new ArrayList<>();
	// 	if (applies.hasApplies(meetingId)) {
	// 		applyWholeInfoDtos = applies.getAppliesMap().get(meetingId).stream()
	// 			.map(apply -> ApplyWholeInfoDto.of(apply, apply.getUser(), userId))
	// 			.toList();
	// 	}
	//
	// 	return LightningV2GetLightningByIdResponseDto.of(meetingId, meeting, coLeaders.getCoLeaders(meetingId),
	// 		isCoLeader,
	// 		approvedCount, isHost, isApply, isApproved,
	// 		meetingLeader, applyWholeInfoDtos, realTime.now());
	// }
}
