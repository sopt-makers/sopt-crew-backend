package org.sopt.makers.crew.main.tag.v2.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.Collections;
import java.util.List;

import org.sopt.makers.crew.main.entity.tag.Tag;
import org.sopt.makers.crew.main.entity.tag.TagRepository;
import org.sopt.makers.crew.main.entity.tag.WelcomeMessageTypeProjection;
import org.sopt.makers.crew.main.entity.tag.enums.TagType;
import org.sopt.makers.crew.main.entity.tag.enums.WelcomeMessageType;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.tag.v2.dto.response.TagV2CreateFlashTagResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagV2ServiceImpl implements TagV2Service {

	private final TagRepository tagRepository;

	// 여기에 createGeneralMeetingTag 메서드도 추가하면 될 것 같습니다 나중에! (추후 일반 모임에 태그 추가 시 작성)

	@Override
	@Transactional
	public TagV2CreateFlashTagResponseDto createFlashTag(List<String> welcomeMessageTypes,
		Integer flashId) {

		if (flashId == null) {
			throw new BadRequestException(VALIDATION_EXCEPTION.getErrorCode());
		}

		if (welcomeMessageTypes == null || welcomeMessageTypes.isEmpty()) {
			return saveTag(flashId, List.of());
		}

		List<WelcomeMessageType> welcomeMessageTypeEnums = welcomeMessageTypes.stream()
			.map(WelcomeMessageType::ofValue)
			.toList();

		return saveTag(flashId, welcomeMessageTypeEnums);
	}

	@Override
	public List<WelcomeMessageType> getWelcomeMessageTypesByFlashId(Integer flashId) {
		if (flashId == null) {
			throw new BadRequestException(VALIDATION_EXCEPTION.getErrorCode());
		}

		return tagRepository.findByFlashId(flashId)
			.map(WelcomeMessageTypeProjection::getWelcomeMessageTypes)
			.orElse(Collections.emptyList());
	}

	private TagV2CreateFlashTagResponseDto saveTag(Integer flashId, List<WelcomeMessageType> welcomeMessageTypeEnums) {
		Tag tag = Tag.createFlashMeetingTag(TagType.FLASH, flashId, welcomeMessageTypeEnums);
		tagRepository.save(tag);
		return TagV2CreateFlashTagResponseDto.from(tag.getId());
	}
}
