package org.sopt.makers.crew.main.external.playground.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sopt.makers.crew.main.external.playground.entity.member_block.MemberBlock;
import org.sopt.makers.crew.main.external.playground.entity.member_block.MemberBlockRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberBlockServiceImpl implements MemberBlockService {

	private final MemberBlockRepository memberBlockRepository;

	@Override
	public Map<Long, Boolean> getBlockedUsers(Long blockerOrgId, List<Long> userOrgIds) {
		List<MemberBlock> memberBlocks = memberBlockRepository.findAllByBlockerAndIsBlockedTrue(blockerOrgId);

		return memberBlocks.stream()
			.filter(memberBlock -> userOrgIds.contains(memberBlock.getBlockedMember()))
			.collect(Collectors.toMap(MemberBlock::getBlockedMember, memberBlock -> true));
	}

	@Override
	public Map<Long, Boolean> getBlockedUsers(Long blockerOrgId) {
		List<MemberBlock> memberBlocks = memberBlockRepository.findAllByBlockerAndIsBlockedTrue(blockerOrgId);

		return memberBlocks.stream()
			.collect(Collectors.toMap(MemberBlock::getBlockedMember, memberBlock -> true));
	}
}