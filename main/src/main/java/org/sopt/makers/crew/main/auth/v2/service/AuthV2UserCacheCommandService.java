package org.sopt.makers.crew.main.auth.v2.service;

import org.sopt.makers.crew.main.global.dto.OrgIdListDto;

public interface AuthV2UserCacheCommandService {
	void evictMeetingLeaderCache(Integer userId);

	void evictCoLeadersCache(Integer meetingId);

	OrgIdListDto refreshOrgIdCache();
}
