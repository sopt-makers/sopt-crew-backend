package org.sopt.makers.crew.main.entity.soptmap;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum SubwayLine {

	// 1~9호선 (별다른 별칭이 없는 경우)
	LINE_1("1호선"),
	LINE_2("2호선"),
	LINE_3("3호선"),
	LINE_4("4호선"),
	LINE_5("5호선"),
	LINE_6("6호선"),
	LINE_7("7호선"),
	LINE_8("8호선"),
	LINE_9("9호선"),

	// 인천 지하철
	INCHEON_1("인천1호선"),
	INCHEON_2("인천2호선"),

	// 경기/광역/기타 노선
	SINBUNDANG("신분당선"),
	SUIN_BUNDANG("수인분당선"),
	GYEONGUI_JUNGANG("경의중앙선"),
	GYEONGCHUN("경춘선"),
	GYEONGGANG("경강선"),

	// 별칭이 필요한 노선들 (가변 인자로 여러 개 등록 가능)
	UI_SINSEOL("우이신설선", "우이신설경전철"),
	SILLIM("신림선", "신림경전철"),

	// 두 이름 모두 처리
	EVERLINE("에버라인", "용인경전철"),

	GIMPO_GOLD("김포골드라인"),
	SEOHAE("서해선"),
	AIRPORT_RAILROAD("공항철도"),
	UIJEONGBU("의정부경전철"),
	GTX_A("GTX-A", "지티엑스A");

	private final String value;       // 대표 명칭 (DB 저장용)
	private final List<String> aliases; // 인식 가능한 다른 이름들

	// 생성자: 첫 번째는 대표 이름, 나머지는 별칭으로 처리
	SubwayLine(String value, String... aliases) {
		this.value = value;
		this.aliases = Arrays.asList(aliases);
	}

	public static List<String> fromValues(List<SubwayLine> lines) {
		return lines.stream().map(SubwayLine::getValue).collect(Collectors.toList());
	}

	/**
	 * JSON 역직렬화 및 문자열 변환 시 사용
	 * 입력값이 대표 이름이거나 별칭 중 하나라면 해당 Enum 반환
	 */
	@JsonCreator
	public static SubwayLine fromValue(String input) {
		if (input == null) {
			throw new IllegalArgumentException("지하철 노선 값이 null입니다.");
		}

		return Arrays.stream(values())
			.filter(line -> line.value.equals(input) || line.aliases.contains(input) || line.name().equals(input))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 지하철 노선입니다: " + input));
	}

	/**
	 * JSON 직렬화 시 사용할 값 (대표 명칭 반환)
	 */
	@JsonValue
	public String getValue() {
		return value;
	}
}
