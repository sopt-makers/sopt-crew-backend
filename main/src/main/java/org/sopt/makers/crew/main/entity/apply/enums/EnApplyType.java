package org.sopt.makers.crew.main.entity.apply.enums;

import java.util.Arrays;

import org.sopt.makers.crew.main.common.exception.BadRequestException;
import org.sopt.makers.crew.main.common.exception.BaseException;
import org.springframework.http.HttpStatus;

/** 지원 구분 */
public enum EnApplyType {
	/** 지원 */
	APPLY(0),

	/** 초대 */
	INVITE(1);

	private final int value;

	EnApplyType(int value) {
		this.value = value;
	}

	public static EnApplyType ofValue(Integer dbData) {
		return Arrays.stream(EnApplyType.values())
			.filter(v -> v.getValue() == (dbData))
			.findFirst()
			.orElseThrow(() -> new BadRequestException(
				String.format("EnApplyType 클래스에 value = [%s] 값을 가진 enum 객체가 없습니다.", dbData)));
	}

	public int getValue() {
		return value;
	}
}
