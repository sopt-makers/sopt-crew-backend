package org.sopt.makers.crew.main.entity.post;

import static org.sopt.makers.crew.main.common.response.ErrorStatus.NOT_FOUND_POST;

import java.util.Optional;
import org.sopt.makers.crew.main.common.exception.BadRequestException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Integer>, PostSearchRepository {

    Optional<Post> findById(Integer postId);

    default Post findByIdOrThrow(Integer postId) {
        return findById(postId)
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_POST.getErrorCode()));
    }

    Optional<Post> findFirstByMeetingIdOrderByIdDesc(Integer meetingId);

    Integer countByMeetingId(Integer meetingId);
}
