package org.sopt.makers.crew.main.tag.v2.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.List;

import org.sopt.makers.crew.main.entity.tag.Tag;
import org.sopt.makers.crew.main.entity.tag.TagRepository;
import org.sopt.makers.crew.main.entity.tag.enums.TagType;
import org.sopt.makers.crew.main.entity.tag.enums.WelcomeMessageType;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.tag.v2.dto.response.TagV2CreateTagResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagV2ServiceImpl implements TagV2Service {

	private final TagRepository tagRepository;

	@Override
	@Transactional
	public TagV2CreateTagResponseDto createLightningTag(List<String> welcomeMessageTypes,
		Integer lightningId) {

		if (lightningId == null) {
			throw new BadRequestException(VALIDATION_EXCEPTION.getErrorCode());
		}

		List<WelcomeMessageType> welcomeMessageTypeEnums = welcomeMessageTypes.stream()
			.map(WelcomeMessageType::ofValue)
			.toList();

		Tag tag = Tag.createLightningMeetingTag(TagType.LIGHTNING, lightningId, welcomeMessageTypeEnums);
		tagRepository.save(tag);

		return TagV2CreateTagResponseDto.from(tag.getId());
	}

	// 여기에 createGeneralMeetingTag 메서드도 추가하면 될 것 같습니다 나중에!
}
