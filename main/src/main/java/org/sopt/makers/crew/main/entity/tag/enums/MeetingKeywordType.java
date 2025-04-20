package org.sopt.makers.crew.main.entity.tag.enums;

import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.ErrorStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MeetingKeywordType {
	EXERCISE("운동"),
	FOOD("먹방"),
	HOBBY("취미"),
	STUDY("학습"),
	SELF_DEVELOPMENT("자기개발"),
	NETWORKING("네트워킹"),
	ETC("기타");

	private final String value;

	public static MeetingKeywordType ofValue(String dbData) {
		for (MeetingKeywordType type : MeetingKeywordType.values()) {
			if (type.getValue().equals(dbData)) {
				return type;
			}
		}
		throw new BadRequestException(ErrorStatus.INVALID_MEETING_KEYWORD_TYPE.getErrorCode());
	}
}
