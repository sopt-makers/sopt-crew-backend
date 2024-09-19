package org.sopt.makers.crew.main.external.playground.entity.member_block;

public interface MemberBlockSearchRepository {
    boolean existsBlockedPost(Long blockedMember, Long blocker, boolean isBlocked);
}