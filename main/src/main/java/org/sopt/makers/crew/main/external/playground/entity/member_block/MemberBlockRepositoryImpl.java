package org.sopt.makers.crew.main.external.playground.entity.member_block;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class MemberBlockRepositoryImpl implements MemberBlockSearchRepository {
    private final JPAQueryFactory queryFactory;

    public MemberBlockRepositoryImpl(@Qualifier("playgroundQueryFactory") JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Map<Long, Boolean> checkBlockedUsers(Long blockerOrgId, List<Long> blockedUserOrgIds) {
        QMemberBlock memberBlock = QMemberBlock.memberBlock;

        // 한 번의 쿼리로 여러 유저의 차단 여부를 확인
        List<Tuple> results = queryFactory
                .select(memberBlock.blockedMember, memberBlock.isBlocked)
                .from(memberBlock)
                .where(memberBlock.blocker.eq(blockerOrgId)
                        .and(memberBlock.blockedMember.in(blockedUserOrgIds)))
                .fetch();

        // 결과를 Map으로 변환
        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(memberBlock.blockedMember),
                        tuple -> tuple.get(memberBlock.isBlocked)
                ));
    }
}