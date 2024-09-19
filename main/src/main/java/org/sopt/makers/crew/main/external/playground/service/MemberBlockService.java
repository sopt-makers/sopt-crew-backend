package org.sopt.makers.crew.main.external.playground.service;

public interface MemberBlockService {
    boolean isBlockedPost(Long blockedUserOrgId, Long blockerOrgId);
}