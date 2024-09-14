package org.sopt.makers.crew.main.external.playground.entity.member_block;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;

@Qualifier("secondaryEntityManagerFactory")
public interface MemberBlockRepository extends JpaRepository<MemberBlock, Long> {
}
