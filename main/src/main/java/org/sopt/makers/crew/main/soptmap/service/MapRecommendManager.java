package org.sopt.makers.crew.main.soptmap.service;

import org.sopt.makers.crew.main.entity.soptmap.MapRecommend;
import org.sopt.makers.crew.main.entity.soptmap.repository.MapRecommendRepository;
import org.sopt.makers.crew.main.soptmap.dto.ToggleSoptMapRecommendDto;
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

	@Transactional
	public ToggleSoptMapRecommendDto toggleRecommend(Long userId, Long soptMapId) {
		return mapRecommendRepository.findFirstByUserIdAndSoptMapId(
				userId, soptMapId).map(mapRecommend -> {
				mapRecommend.toggleStatus();
				return ToggleSoptMapRecommendDto.from(mapRecommend);
			})
			.orElseGet(() -> ToggleSoptMapRecommendDto.from(
				mapRecommendRepository.save(MapRecommend.create(userId, soptMapId))));

	}
}
