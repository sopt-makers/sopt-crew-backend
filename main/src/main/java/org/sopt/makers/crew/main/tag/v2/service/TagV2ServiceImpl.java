package org.sopt.makers.crew.main.tag.v2.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.Collections;
import java.util.List;

import org.sopt.makers.crew.main.entity.tag.Tag;
import org.sopt.makers.crew.main.entity.tag.TagRepository;
import org.sopt.makers.crew.main.entity.tag.WelcomeMessageTypeProjection;
import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;
import org.sopt.makers.crew.main.entity.tag.enums.TagType;
import org.sopt.makers.crew.main.entity.tag.enums.WelcomeMessageType;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.sopt.makers.crew.main.tag.v2.dto.response.TagV2CreateAndUpdateFlashTagResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagV2ServiceImpl implements TagV2Service {

	private static final int MAX_MEETING_KEYWORD_SIZE = 2;
	private final TagRepository tagRepository;

	// 여기에 createGeneralMeetingTag 메서드도 추가하면 될 것 같습니다 나중에! (추후 일반 모임에 태그 추가 시 작성)

	@Override
	@Transactional
	public TagV2CreateAndUpdateFlashTagResponseDto createFlashTag(List<String> welcomeMessageTypes,
		List<String> meetingKeywordTypes,
		Integer flashId) {
		if (flashId == null) {
			throw new BadRequestException(VALIDATION_EXCEPTION.getErrorCode());
		}

		List<MeetingKeywordType> meetingKeywordTypeEnums = toMeetingKeywordTypes(meetingKeywordTypes);
		List<WelcomeMessageType> welcomeMessageTypeEnums = toWelcomeMessageTypes(welcomeMessageTypes);

		return saveTag(flashId, welcomeMessageTypeEnums, meetingKeywordTypeEnums);
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

	@Override
	@Transactional
	public TagV2CreateAndUpdateFlashTagResponseDto updateFlashTag(List<String> welcomeMessageTypes,
		List<String> meetingKeywordTypes,
		Integer flashId) {
		Tag tag = tagRepository.findTagByFlashId(flashId)
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_TAG.getErrorCode()));

		List<WelcomeMessageType> welcomeMessageTypeEnums = toWelcomeMessageTypes(welcomeMessageTypes);
		List<MeetingKeywordType> meetingKeywordTypeEnums = toMeetingKeywordTypes(meetingKeywordTypes);

		tag.updateWelcomeMessageTypes(welcomeMessageTypeEnums);
		tag.updateMeetingKeywordTypeEnums(meetingKeywordTypeEnums);

		return TagV2CreateAndUpdateFlashTagResponseDto.from(tag.getId());
	}

	private TagV2CreateAndUpdateFlashTagResponseDto saveTag(Integer flashId,
		List<WelcomeMessageType> welcomeMessageTypes,
		List<MeetingKeywordType> meetingKeywordTypes) {
		Tag tag = Tag.createFlashMeetingTag(TagType.FLASH, flashId, welcomeMessageTypes, meetingKeywordTypes);
		tagRepository.save(tag);
		return TagV2CreateAndUpdateFlashTagResponseDto.from(tag.getId());
	}

	private List<WelcomeMessageType> toWelcomeMessageTypes(List<String> values) {
		if (values == null || values.isEmpty()) {
			return List.of();
		}
		return values.stream()
			.map(WelcomeMessageType::ofValue)
			.toList();
	}

	private List<MeetingKeywordType> toMeetingKeywordTypes(List<String> values) {
		if (values == null || values.isEmpty() || values.size() > MAX_MEETING_KEYWORD_SIZE) {
			throw new BadRequestException(INVALID_MEETING_KEYWORD_SIZE.getErrorCode());
		}
		return values.stream()
			.map(MeetingKeywordType::ofValue)
			.toList();
	}
}
