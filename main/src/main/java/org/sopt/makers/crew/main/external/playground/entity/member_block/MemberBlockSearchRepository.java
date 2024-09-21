package org.sopt.makers.crew.main.external.playground.entity.member_block;

import java.util.List;

import com.querydsl.core.Tuple;

public interface MemberBlockSearchRepository {
	List<Tuple> checkBlockedUsers(Long blockerOrgId, List<Long> userOrgIds);
}