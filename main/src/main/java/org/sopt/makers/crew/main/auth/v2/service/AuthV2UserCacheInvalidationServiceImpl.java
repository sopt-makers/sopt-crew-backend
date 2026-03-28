package org.sopt.makers.crew.main.auth.v2.service;

import org.sopt.makers.crew.main.entity.meeting.CoLeaderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthV2UserCacheInvalidationServiceImpl implements AuthV2UserCacheInvalidationService {

	private final CoLeaderRepository coLeaderRepository;
	private final AuthV2UserCacheCommandService authV2UserCacheCommandService;

	@Override
	public void refreshCachesAfterUserUpdate(Integer userId) {
		authV2UserCacheCommandService.evictMeetingLeaderCache(userId);

		coLeaderRepository.findAllByUserIdWithMeeting(userId).stream()
			.map(coLeader -> coLeader.getMeeting().getId())
		.distinct()
			.forEach(authV2UserCacheCommandService::evictCoLeadersCache);

		authV2UserCacheCommandService.refreshOrgIdCache();

		log.info("사용자 정보 변경 후 캐시 갱신 완료: userId={}", userId);
	}

	@Override
	public void refreshCachesAfterUserCreate() {
		authV2UserCacheCommandService.refreshOrgIdCache();
		log.info("신규 사용자 생성 후 orgId 캐시 갱신 완료");
	}
}
