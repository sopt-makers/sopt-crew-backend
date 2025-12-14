package org.sopt.makers.crew.main.soptmap.service;

import org.sopt.makers.crew.main.soptmap.dto.request.SoptMapRequest.CreateSoptMapRequest;
import org.springframework.stereotype.Component;

@Component
public class SoptMapRequestValidator {

	public void validate(CreateSoptMapRequest request) {
		if (request.getTags().size() > 2)
			throw new IllegalArgumentException("Tags must contain at most 2 tags");
		if (request.getStationNames().size() > 3)
			throw new IllegalArgumentException("Station must contain at most 3 tags");
	}

}
