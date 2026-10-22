package org.sopt.makers.crew.main.entity.dining;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.sopt.makers.crew.main.global.exception.NotFoundException;

/**
 * 한 회식의 회차 목록 (1차부터 id 오름차순)
 * 회차 번호는 저장하지 않고 이 순서로 계산한다. (인덱스 + 1 = 회차 번호)
 */
public class DiningRounds {

	private static final long NEW_ROUND_HOURS = 24;

	private final List<DiningRound> rounds;

	public DiningRounds(List<DiningRound> rounds) {
		this.rounds = rounds.stream()
			.sorted(Comparator.comparing(DiningRound::getId))
			.toList();
	}

	public List<DiningRound> getRounds() {
		return rounds;
	}

	public List<Integer> getRoundIds() {
		return rounds.stream()
			.map(DiningRound::getId)
			.toList();
	}

	public DiningRound getRound(Integer roundId) {
		return rounds.stream()
			.filter(round -> round.getId().equals(roundId))
			.findFirst()
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_DINING_ROUND.getErrorCode()));
	}

	public int getRoundNumber(Integer roundId) {
		return rounds.indexOf(getRound(roundId)) + 1;
	}

	public DiningRound getFirstRound() {
		if (rounds.isEmpty()) {
			throw new NotFoundException(NOT_FOUND_DINING_ROUND.getErrorCode());
		}
		return rounds.get(0);
	}

	public boolean isFirstRound(DiningRound round) {
		return !rounds.isEmpty() && rounds.get(0).getId().equals(round.getId());
	}

	public boolean hasClosedRound() {
		return rounds.stream().anyMatch(DiningRound::isClosed);
	}

	public List<DiningRound> getOpenRounds() {
		return rounds.stream()
			.filter(round -> !round.isClosed())
			.toList();
	}

	/**
	 * NEW 표시 여부. 1차를 제외한 가장 최신 회차가 생성된 지 24시간 이내일 때만 true
	 */
	public boolean isNewRound(DiningRound round, LocalDateTime now) {
		if (rounds.size() < 2) {
			return false;
		}
		DiningRound latestRound = rounds.get(rounds.size() - 1);
		if (!latestRound.getId().equals(round.getId()) || latestRound.createdTimestamp == null) {
			return false;
		}
		return now.isBefore(latestRound.createdTimestamp.plusHours(NEW_ROUND_HOURS));
	}
}
