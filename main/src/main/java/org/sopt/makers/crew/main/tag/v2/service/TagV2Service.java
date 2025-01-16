package org.sopt.makers.crew.main.tag.v2.service;

import java.util.List;

import org.sopt.makers.crew.main.entity.tag.enums.WelcomeMessageType;
import org.sopt.makers.crew.main.tag.v2.dto.response.TagV2CreateTagResponseDto;

public interface TagV2Service {
	TagV2CreateTagResponseDto createLightningTag(List<WelcomeMessageType> welcomeMessageTypes, Integer lightningId);
}
