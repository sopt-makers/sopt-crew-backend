package org.sopt.makers.crew.main.external.playground.entity.member_block;

import java.util.List;
import java.util.Map;

public interface MemberBlockSearchRepository {
    Map<Long, Boolean> checkBlockedUsers(Long blockerOrgId, List<Long> blockedUserOrgIds);
}