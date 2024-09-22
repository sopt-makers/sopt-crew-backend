package org.sopt.makers.crew.main.external.playground.entity.member_block;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberBlockRepository extends JpaRepository<MemberBlock, Long> {
	List<MemberBlock> findAllByBlockerAndIsBlockedTrue(Long orgId);
}