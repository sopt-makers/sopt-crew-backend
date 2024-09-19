package org.sopt.makers.crew.main.external.playground.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.external.playground.entity.member_block.MemberBlockRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberBlockServiceImpl implements MemberBlockService {

    private final MemberBlockRepository memberBlockRepository;

    @Override
    public boolean isBlockedPost(Long blockedUserOrgId, Long blockerOrgId) {
        return memberBlockRepository.existsBlockedPost(blockedUserOrgId, blockerOrgId, true);
    }
}