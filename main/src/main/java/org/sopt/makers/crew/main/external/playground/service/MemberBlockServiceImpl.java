package org.sopt.makers.crew.main.external.playground.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sopt.makers.crew.main.external.playground.entity.member_block.MemberBlockRepository;
import org.springframework.stereotype.Service;

import com.querydsl.core.Tuple;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberBlockServiceImpl implements MemberBlockService {

	private final MemberBlockRepository memberBlockRepository;

	@Override
	public Map<Long, Boolean> getBlockedUsers(Long blockerOrgId, List<Long> userOrgIds) {
		List<Tuple> results = memberBlockRepository.checkBlockedUsers(blockerOrgId, userOrgIds);

		return results.stream()
			.collect(Collectors.toMap(
				tuple -> tuple.get(0, Long.class),
				tuple -> tuple.get(1, Boolean.class)
			));
	}
}