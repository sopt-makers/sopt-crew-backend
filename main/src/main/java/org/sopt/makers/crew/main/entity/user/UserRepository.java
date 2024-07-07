package org.sopt.makers.crew.main.entity.user;

import static org.sopt.makers.crew.main.common.response.ErrorStatus.NO_CONTENT_EXCEPTION;

import java.util.List;
import java.util.Optional;
import org.sopt.makers.crew.main.common.exception.NoContentException;
import org.sopt.makers.crew.main.common.exception.UnAuthorizedException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByOrgId(Integer orgId);

    default User findByIdOrThrow(Integer userId) {
        return findById(userId).orElseThrow(() -> new UnAuthorizedException());
    }

    default User findByOrgIdOrThrow(Integer orgUserId) {
        return findByOrgId(orgUserId).orElseThrow(
            () -> new NoContentException(
                NO_CONTENT_EXCEPTION.getErrorCode())); //유저가 아직 모임 서비스를 이용 전이기 때문에
    }

    List<User> findByIdIn(List<Integer> userIds);
}