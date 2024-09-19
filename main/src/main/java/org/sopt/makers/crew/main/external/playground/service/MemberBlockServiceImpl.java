package org.sopt.makers.crew.main.external.playground.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.external.playground.entity.member_block.MemberBlockRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberBlockServiceImpl implements MemberBlockService {

    private final MemberBlockRepository memberBlockRepository;

    @Override
    public Map<Long, Boolean> getBlockedUsers(Long blockerOrgId, List<Long> blockedUserOrgIds) {
        return memberBlockRepository.checkBlockedUsers(blockerOrgId, blockedUserOrgIds);
    }
}