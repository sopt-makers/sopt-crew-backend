package org.sopt.makers.crew.main.auth.v2.service;

import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.global.dto.OrgIdListDto;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthV2UserCacheCommandServiceImpl implements AuthV2UserCacheCommandService {

	private final UserRepository userRepository;

	@Override
	@CacheEvict(value = "meetingLeaderCache", key = "#userId")
	public void evictMeetingLeaderCache(Integer userId) {
	}

	@Override
	@CacheEvict(value = "coLeadersCache", key = "#meetingId")
	public void evictCoLeadersCache(Integer meetingId) {
	}

	@Override
	@CachePut(value = "orgIdCache", key = "'allOrgIds'")
	public OrgIdListDto refreshOrgIdCache() {
		OrgIdListDto latestOrgIds = OrgIdListDto.of(userRepository.findAllOrgIds());

		log.info("orgId 캐시 갱신 완료: {}", latestOrgIds);

		return latestOrgIds;
	}
}
