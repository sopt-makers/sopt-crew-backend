package org.sopt.makers.crew.main.admin.v2.dto.mumutext;

import java.time.LocalDateTime;
import java.util.List;

public record AdminMumuTextBulkPreviewRow(
	int rowNumber,
	String text,
	String category,
	LocalDateTime showStartDate,
	LocalDateTime showEndDate,
	boolean valid,
	List<String> messages
) {
	public static AdminMumuTextBulkPreviewRow valid(int rowNumber, String text, String category,
		LocalDateTime showStartDate, LocalDateTime showEndDate) {
		return new AdminMumuTextBulkPreviewRow(rowNumber, text, category, showStartDate, showEndDate, true, List.of());
	}

	public static AdminMumuTextBulkPreviewRow invalid(int rowNumber, String text, String category,
		LocalDateTime showStartDate, LocalDateTime showEndDate, List<String> messages) {
		return new AdminMumuTextBulkPreviewRow(rowNumber, text, category, showStartDate, showEndDate, false, messages);
	}
}
