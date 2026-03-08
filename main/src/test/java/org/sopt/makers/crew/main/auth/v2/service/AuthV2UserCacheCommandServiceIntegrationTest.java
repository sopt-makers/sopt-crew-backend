package org.sopt.makers.crew.main.auth.v2.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.external.CaffeineTestConfig;
import org.sopt.makers.crew.main.external.caffeine.CaffeineConfig;
import org.sopt.makers.crew.main.global.dto.OrgIdListDto;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = AuthV2UserCacheCommandServiceImpl.class)
@Import({CaffeineConfig.class, CaffeineTestConfig.class})
@ActiveProfiles("test")
class AuthV2UserCacheCommandServiceIntegrationTest {

	@Autowired
	private AuthV2UserCacheCommandService authV2UserCacheCommandService;

	@Autowired
	private CacheManager cacheManager;

	@MockBean
	private UserRepository userRepository;

	@BeforeEach
	void setUp() {
		cacheManager.getCacheNames().forEach(cacheName -> cacheManager.getCache(cacheName).clear());
	}

	@Test
	@DisplayName("meetingLeader cache eviction 테스트")
	void meetingLeader_cache_eviction이_프록시를_통해_실제_적용된다() {
		Cache cache = cacheManager.getCache("meetingLeaderCache");
		cache.put(1, "leader");

		authV2UserCacheCommandService.evictMeetingLeaderCache(1);

		assertThat(cache.get(1)).isNull();
	}

	@Test
	@DisplayName("coLeaders cache eviction 테스트")
	void coLeaders_cache_eviction이_프록시를_통해_실제_적용된다() {
		Cache cache = cacheManager.getCache("coLeadersCache");
		cache.put(10, "co-leaders");

		authV2UserCacheCommandService.evictCoLeadersCache(10);

		assertThat(cache.get(10)).isNull();
	}

	@Test
	@DisplayName("orgId cache put 테스트")
	void orgId_cache_put이_프록시를_통해_실제_적용된다() {
		doReturn(List.of(1, 2, 3)).when(userRepository).findAllOrgIds();

		OrgIdListDto result = authV2UserCacheCommandService.refreshOrgIdCache();
		Cache.ValueWrapper cachedValue = cacheManager.getCache("orgIdCache").get("allOrgIds");

		assertThat(result.getOrgIds()).containsExactly(1, 2, 3);
		assertThat(cachedValue).isNotNull();
		assertThat(((OrgIdListDto)cachedValue.get()).getOrgIds()).containsExactly(1, 2, 3);
	}
}
