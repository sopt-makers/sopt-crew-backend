package org.sopt.makers.crew.main.entity.apply.enums;

import java.util.Arrays;
import org.sopt.makers.crew.main.common.exception.BadRequestException;
import org.sopt.makers.crew.main.common.exception.BaseException;
import org.springframework.http.HttpStatus;

/** 신청 상태 */
public enum EnApplyStatus {
    /** 대기 */
    WAITING(0),

    /** 승인된 신청자 */
    APPROVE(1),

    /** 거절된 신청자 */
    REJECT(2);

    private final int value;

    EnApplyStatus(int value) {
        this.value = value;
    }

    public static EnApplyStatus ofValue(int dbData) {
        return Arrays.stream(EnApplyStatus.values())
                .filter(v -> v.getValue()==(dbData))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        String.format("EnApplyStatus 클래스에 value = [%s] 값을 가진 enum 객체가 없습니다.", dbData)));
    }

    public int getValue() {
        return value;
    }
}
