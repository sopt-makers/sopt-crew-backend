package org.sopt.makers.crew.main.entity.post;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MumuTextResolver {

	private final MumuTextRepository mumuTextRepository;

	public MumuText resolveMumuText(LocalDateTime dateTime) {
		return mumuTextRepository.findByInDateTimeText(dateTime)
			.orElseGet(() -> resolveRepeatedText(dateTime));
	}

	private MumuText resolveRepeatedText(LocalDateTime dateTime) {
		List<MumuText> mumuTexts = mumuTextRepository.findAllByOrderByShowStartDateAsc();
		if (mumuTexts.isEmpty()) {
			throw new IllegalArgumentException("No mumu texts found");
		}

		long days = ChronoUnit.DAYS.between(mumuTexts.get(0).getShowStartDate().toLocalDate(), dateTime.toLocalDate());
		int index = Math.floorMod(days, mumuTexts.size());

		return mumuTexts.get(index);
	}
}
