package org.sopt.makers.crew.main.lightning.v2.service;

import static org.sopt.makers.crew.main.global.constant.CrewConst.*;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import org.sopt.makers.crew.main.entity.lightning.Lightning;
import org.sopt.makers.crew.main.entity.lightning.LightningRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.lightning.v2.dto.mapper.LightningMapper;
import org.sopt.makers.crew.main.lightning.v2.dto.request.LightningV2CreateLightningBodyDto;
import org.sopt.makers.crew.main.lightning.v2.dto.response.LightningV2CreateLightningResponseDto;
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

	private final LightningRepository lightningRepository;
	private final LightningMapper lightningMapper;

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

		Lightning lightning = lightningMapper.toLightningEntity(requestBody.lightningBody(), ACTIVE_GENERATION,
			user.getId());

		lightningRepository.save(lightning);
		tagV2Service.createLightningTag(requestBody.welcomeMessageTypes(), lightning.getId());

		return LightningV2CreateLightningResponseDto.from(lightning.getId());
	}
}
