package org.sopt.makers.crew.main.tag.v2.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.Collections;
import java.util.List;

import org.sopt.makers.crew.main.entity.tag.Tag;
import org.sopt.makers.crew.main.entity.tag.TagRepository;
import org.sopt.makers.crew.main.entity.tag.WelcomeMessageTypeProjection;
import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;
import org.sopt.makers.crew.main.entity.tag.enums.WelcomeMessageType;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.sopt.makers.crew.main.tag.v2.dto.response.TagV2CreateFlashTagResponseDto;
import org.sopt.makers.crew.main.tag.v2.dto.response.TagV2CreateGeneralMeetingTagResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagV2ServiceImpl implements TagV2Service {

	private static final int MAX_MEETING_KEYWORD_SIZE = 2;
	private final TagRepository tagRepository;

	@Override
	@Transactional
	public TagV2CreateGeneralMeetingTagResponseDto createGeneralMeetingTag(List<String> welcomeMessageTypes,
		List<String> meetingKeywordTypes, Integer meetingId) {
		if (meetingId == null) {
			throw new BadRequestException(VALIDATION_EXCEPTION.getErrorCode());
		}

		List<WelcomeMessageType> welcomeMessageTypeEnums = toWelcomeMessageTypes(welcomeMessageTypes);
		List<MeetingKeywordType> meetingKeywordTypeEnums = toMeetingKeywordTypes(meetingKeywordTypes);

		return saveGeneralMeetingTag(meetingId, welcomeMessageTypeEnums, meetingKeywordTypeEnums);
	}

	@Override
	@Transactional
	public TagV2CreateFlashTagResponseDto createFlashMeetingTag(List<String> welcomeMessageTypes,
		List<String> meetingKeywordTypes, Integer flashId) {
		if (flashId == null) {
			throw new BadRequestException(VALIDATION_EXCEPTION.getErrorCode());
		}

		List<WelcomeMessageType> welcomeMessageTypeEnums = toWelcomeMessageTypes(welcomeMessageTypes);
		List<MeetingKeywordType> meetingKeywordTypeEnums = toMeetingKeywordTypes(meetingKeywordTypes);

		return saveFlashMeetingTag(flashId, welcomeMessageTypeEnums, meetingKeywordTypeEnums);
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
	public void updateGeneralMeetingTag(List<String> welcomeMessageTypes,
		List<String> meetingKeywordTypes, Integer meetingId) {
		Tag tag = tagRepository.findTagByMeetingId(meetingId)
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_TAG.getErrorCode()));

		List<WelcomeMessageType> welcomeMessageTypeEnums = toWelcomeMessageTypes(welcomeMessageTypes);
		List<MeetingKeywordType> meetingKeywordTypeEnums = toMeetingKeywordTypes(meetingKeywordTypes);

		tag.updateWelcomeMessageTypes(welcomeMessageTypeEnums);
		tag.updateMeetingKeywordTypeEnums(meetingKeywordTypeEnums);
	}

	@Override
	@Transactional
	public void updateFlashMeetingTag(List<String> welcomeMessageTypes,
		List<String> meetingKeywordTypes, Integer flashId) {
		Tag tag = tagRepository.findTagByFlashId(flashId)
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_TAG.getErrorCode()));

		List<WelcomeMessageType> welcomeMessageTypeEnums = toWelcomeMessageTypes(welcomeMessageTypes);
		List<MeetingKeywordType> meetingKeywordTypeEnums = toMeetingKeywordTypes(meetingKeywordTypes);

		tag.updateWelcomeMessageTypes(welcomeMessageTypeEnums);
		tag.updateMeetingKeywordTypeEnums(meetingKeywordTypeEnums);
	}

	private TagV2CreateGeneralMeetingTagResponseDto saveGeneralMeetingTag(Integer meetingId,
		List<WelcomeMessageType> welcomeMessageTypes, List<MeetingKeywordType> meetingKeywordTypes) {
		Tag tag = Tag.createGeneralMeetingTag(meetingId, welcomeMessageTypes, meetingKeywordTypes);
		tagRepository.save(tag);
		return TagV2CreateGeneralMeetingTagResponseDto.from(tag.getId());
	}

	private TagV2CreateFlashTagResponseDto saveFlashMeetingTag(Integer flashId,
		List<WelcomeMessageType> welcomeMessageTypes, List<MeetingKeywordType> meetingKeywordTypes) {
		Tag tag = Tag.createFlashMeetingTag(flashId, welcomeMessageTypes, meetingKeywordTypes);
		tagRepository.save(tag);
		return TagV2CreateFlashTagResponseDto.from(tag.getId());
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
