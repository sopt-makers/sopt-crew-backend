package org.sopt.makers.crew.main.external.playground.entity.member_block;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class MemberBlockSearchRepositoryImpl implements MemberBlockSearchRepository {
	private final JPAQueryFactory queryFactory;

	public MemberBlockSearchRepositoryImpl(@Qualifier("playgroundQueryFactory") JPAQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	@Override
	public List<Tuple> checkBlockedUsers(Long blockerOrgId, List<Long> userOrgIds) {
		QMemberBlock memberBlock = QMemberBlock.memberBlock;

		List<Tuple> blockedUserTuples = queryFactory.select(memberBlock.blockedMember, memberBlock.isBlocked)
			.from(memberBlock)
			.where(memberBlock.blocker.eq(blockerOrgId).and(memberBlock.blockedMember.in(userOrgIds)))
			.fetch();

		return blockedUserTuples;
	}
}