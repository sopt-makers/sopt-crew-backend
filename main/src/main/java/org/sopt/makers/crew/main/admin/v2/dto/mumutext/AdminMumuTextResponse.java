package org.sopt.makers.crew.main.admin.v2.dto.mumutext;

import java.time.LocalDateTime;

import org.sopt.makers.crew.main.entity.post.MumuText;

public record AdminMumuTextResponse(
	Long id,
	String text,
	String category,
	LocalDateTime showStartDate,
	LocalDateTime showEndDate,
	AdminMumuTextStatus status
) {
	public static AdminMumuTextResponse from(MumuText mumuText, LocalDateTime now) {
		return new AdminMumuTextResponse(
			mumuText.getId(),
			mumuText.getText(),
			mumuText.getCategory(),
			mumuText.getShowStartDate(),
			mumuText.getShowEndDate(),
			AdminMumuTextStatus.from(now, mumuText.getShowStartDate(), mumuText.getShowEndDate())
		);
	}
}
