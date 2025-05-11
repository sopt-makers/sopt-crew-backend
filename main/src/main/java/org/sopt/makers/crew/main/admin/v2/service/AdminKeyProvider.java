package org.sopt.makers.crew.main.admin.v2.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Component
@RequiredArgsConstructor
public class AdminKeyProvider {

	@Value("${custom.paths.adminKey}")
	private String adminKey;

}
