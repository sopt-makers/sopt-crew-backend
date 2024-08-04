package org.sopt.makers.crew.main.entity.like;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface LikeRepository extends JpaRepository<Like, Integer> {

    List<Like> findAllByUserIdAndPostIdNotNull(Integer userId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM Like l WHERE l.postId = :postId")
    void deleteAllByPostId(Integer postId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM Like l WHERE l.commentId IN :commentIds")
    void deleteAllByIdsInQuery(List<Integer> commentIds);
}
