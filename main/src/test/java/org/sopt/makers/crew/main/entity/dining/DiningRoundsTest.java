package org.sopt.makers.crew.main.entity.dining;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class DiningRoundsTest {

	private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 23, 12, 0);

	private DiningRound round(Integer id, LocalDateTime createdTimestamp) {
		DiningRound round = DiningRound.builder().meetingId(1).build();
		ReflectionTestUtils.setField(round, "id", id);
		round.createdTimestamp = createdTimestamp;
		return round;
	}

	@Test
	@DisplayName("회차 번호는 id 오름차순으로 계산하고, 중간 회차가 빠지면 뒤 번호가 당겨진다.")
	void getRoundNumber_isOrderedById() {
		DiningRounds rounds = new DiningRounds(List.of(round(30, NOW), round(10, NOW), round(50, NOW)));

		assertThat(rounds.getRoundNumber(10)).isEqualTo(1);
		assertThat(rounds.getRoundNumber(30)).isEqualTo(2);
		assertThat(rounds.getRoundNumber(50)).isEqualTo(3);
		assertThat(rounds.isFirstRound(rounds.getRound(10))).isTrue();
	}

	@Test
	@DisplayName("NEW 는 1차를 제외한 최신 회차가 생성 24시간 이내일 때만 표시된다.")
	void isNewRound_onlyLatestNonFirstRoundWithin24Hours() {
		DiningRound first = round(1, NOW.minusHours(1));
		DiningRound second = round(2, NOW.minusHours(23));
		DiningRound third = round(3, NOW.minusHours(1));

		// 1차만 있으면 NEW 없음
		assertThat(new DiningRounds(List.of(first)).isNewRound(first, NOW)).isFalse();

		// 최신 회차(3차)만 NEW, 24시간 이내인 2차라도 최신이 아니면 NEW 아님
		DiningRounds rounds = new DiningRounds(List.of(first, second, third));
		assertThat(rounds.isNewRound(third, NOW)).isTrue();
		assertThat(rounds.isNewRound(second, NOW)).isFalse();
		assertThat(rounds.isNewRound(first, NOW)).isFalse();
	}

	@Test
	@DisplayName("최신 회차라도 생성 후 24시간이 지나면 NEW 가 아니다.")
	void isNewRound_falseAfter24Hours() {
		DiningRound first = round(1, NOW.minusDays(3));
		DiningRound justBefore = round(2, NOW.minusHours(24).plusSeconds(1));
		DiningRound exactly24h = round(3, NOW.minusHours(24));

		assertThat(new DiningRounds(List.of(first, justBefore)).isNewRound(justBefore, NOW)).isTrue();
		assertThat(new DiningRounds(List.of(first, exactly24h)).isNewRound(exactly24h, NOW)).isFalse();
	}
}
