package org.sopt.makers.crew.main.soptmap.service;

import org.sopt.makers.crew.main.entity.soptmap.repository.MapRecommendRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MapRecommendManager {

	private final MapRecommendRepository mapRecommendRepository;

	@Transactional
	public void deleteAllBySoptMapId(Long soptMapId) {
		mapRecommendRepository.deleteAllBySoptMapId(soptMapId);
	}
}
