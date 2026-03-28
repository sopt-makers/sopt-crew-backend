package org.sopt.makers.crew.main.auth.v2.service;

public interface AuthV2UserCacheInvalidationService {
	void refreshCachesAfterUserUpdate(Integer userId);

	void refreshCachesAfterUserCreate();
}
