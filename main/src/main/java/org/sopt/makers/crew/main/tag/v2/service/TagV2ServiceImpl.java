package org.sopt.makers.crew.main.tag.v2.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.sopt.makers.crew.main.entity.tag.Tag;
import org.sopt.makers.crew.main.entity.tag.TagRepository;
import org.sopt.makers.crew.main.entity.tag.enums.TagType;
import org.sopt.makers.crew.main.entity.tag.enums.WelcomeMessageType;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.sopt.makers.crew.main.tag.v2.dto.response.TagV2CreateTagResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagV2ServiceImpl implements TagV2Service {

	private static final String JSON_VALUE_SEPARATOR = ",";

	private final TagRepository tagRepository;

	// 여기에 createGeneralMeetingTag 메서드도 추가하면 될 것 같습니다 나중에! (추후 일반 모임에 태그 추가 시 작성)

	@Override
	@Transactional
	public TagV2CreateTagResponseDto createLightningTag(List<String> welcomeMessageTypes,
		Integer lightningId) {

		if (lightningId == null) {
			throw new BadRequestException(VALIDATION_EXCEPTION.getErrorCode());
		}

		List<WelcomeMessageType> welcomeMessageTypeEnums = null;

		if (welcomeMessageTypes != null) {
			welcomeMessageTypeEnums = welcomeMessageTypes.stream()
				.map(WelcomeMessageType::ofValue)
				.toList();
		}

		Tag tag = Tag.createLightningMeetingTag(TagType.LIGHTNING, lightningId, welcomeMessageTypeEnums);
		tagRepository.save(tag);

		return TagV2CreateTagResponseDto.from(tag.getId());
	}

	@Override
	public List<WelcomeMessageType> getWelcomeMessageTypesByLightningId(Integer lightningId) {
		validateLightningId(lightningId);

		String jsonWelcomeMessageTypes = tagRepository.findWelcomeMessageTypesByLightningId(lightningId)
			.orElseThrow(() -> new NotFoundException(TAG_NOT_FOUND_EXCEPTION.getErrorCode()));

		return parseWelcomeMessageTypes(jsonWelcomeMessageTypes);
	}

	private void validateLightningId(Integer lightningId) {
		if (lightningId == null) {
			throw new BadRequestException(VALIDATION_EXCEPTION.getErrorCode());
		}
	}

	private List<WelcomeMessageType> parseWelcomeMessageTypes(String jsonWelcomeMessageTypes) {
		List<String> values = splitAndTrimJsonValues(jsonWelcomeMessageTypes);

		return values.stream()
			.map(this::convertToWelcomeMessageType)
			.filter(Objects::nonNull)
			.toList();
	}

	private List<String> splitAndTrimJsonValues(String jsonWelcomeMessageTypes) {
		return Arrays.stream(jsonWelcomeMessageTypes.split(JSON_VALUE_SEPARATOR))
			.map(String::trim)
			.toList();
	}

	private WelcomeMessageType convertToWelcomeMessageType(String value) {
		try {
			return WelcomeMessageType.valueOf(value);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
