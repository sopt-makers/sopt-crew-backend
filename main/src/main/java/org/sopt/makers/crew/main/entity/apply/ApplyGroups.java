package org.sopt.makers.crew.main.entity.apply;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;

@RequiredArgsConstructor
@Getter
public class ApplyGroups {
    private final List<Apply> applyList;

    public long getApprovedCount() {
        return applyList.stream()
                .filter(apply -> apply.getStatus().equals(EnApplyStatus.APPROVE))
                .count();
    }

    public Boolean isApply(Integer userId) {
        return applyList.stream()
                .anyMatch(apply -> apply.getUserId().equals(userId));
    }

    public Boolean isApproved(Integer userId) {
        return applyList.stream()
                .anyMatch(apply -> apply.getUserId().equals(userId)
                        && apply.getStatus().equals(EnApplyStatus.APPROVE));
    }

}
