package org.sopt.makers.crew.main.external.playground.entity.member_block;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MemberBlockRepositoryImpl implements MemberBlockSearchRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsBlockedPost(Long blockedMember, Long blocker, boolean isBlocked) {
        QMemberBlock memberBlock = QMemberBlock.memberBlock;

        return queryFactory
                .selectOne()
                .from(memberBlock)
                .where(memberBlock.blockedMember.eq(blockedMember)
                        .and(memberBlock.blocker.eq(blocker))
                        .and(memberBlock.isBlocked.eq(isBlocked)))
                .fetchFirst() != null;
    }
}