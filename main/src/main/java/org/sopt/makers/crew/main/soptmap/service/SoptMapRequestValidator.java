package org.sopt.makers.crew.main.soptmap.service;

import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.ErrorStatus;
import org.sopt.makers.crew.main.soptmap.dto.request.SoptMapRequest.Validatable;
import org.springframework.stereotype.Component;

@Component
public class SoptMapRequestValidator {

	private static final int MAX_TAGS_COUNT = 2;
	private static final int MAX_STATIONS_COUNT = 3;

	public void validate(Validatable request) {
		if (request.getTags().size() > MAX_TAGS_COUNT) {
			throw new BadRequestException(ErrorStatus.INVALID_ARGUMENT.getErrorCode());
		}
		if (request.getStationNames().size() > MAX_STATIONS_COUNT) {
			throw new BadRequestException(ErrorStatus.INVALID_ARGUMENT.getErrorCode());
		}
	}

}
