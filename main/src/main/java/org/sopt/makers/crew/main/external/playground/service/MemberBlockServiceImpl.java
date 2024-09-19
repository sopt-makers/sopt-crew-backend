package org.sopt.makers.crew.main.external.playground.service;

import java.util.List;
import java.util.Map;

import org.sopt.makers.crew.main.external.playground.entity.member_block.MemberBlockRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberBlockServiceImpl implements MemberBlockService {

	private final MemberBlockRepository memberBlockRepository;

	@Override
	public Map<Long, Boolean> getBlockedUsers(Long blockerOrgId, List<Long> userOrgIds) {
		return memberBlockRepository.checkBlockedUsers(blockerOrgId, userOrgIds);
	}
}