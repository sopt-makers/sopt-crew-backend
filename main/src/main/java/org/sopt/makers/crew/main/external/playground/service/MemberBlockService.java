package org.sopt.makers.crew.main.external.playground.service;

import java.util.List;
import java.util.Map;

public interface MemberBlockService {
    Map<Long, Boolean> getBlockedUsers(Long blockerOrgId, List<Long> blockedUserOrgIds);
}