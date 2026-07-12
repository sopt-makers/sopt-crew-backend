package org.sopt.makers.crew.main.admin.v2.dto.mumutext;

import java.util.List;

public record AdminMumuTextBulkPreviewResponse(
	int totalCount,
	int validCount,
	int invalidCount,
	boolean valid,
	List<AdminMumuTextBulkPreviewRow> rows
) {
	public static AdminMumuTextBulkPreviewResponse from(List<AdminMumuTextBulkPreviewRow> rows) {
		int validCount = (int)rows.stream()
			.filter(AdminMumuTextBulkPreviewRow::valid)
			.count();
		int invalidCount = rows.size() - validCount;
		return new AdminMumuTextBulkPreviewResponse(rows.size(), validCount, invalidCount, invalidCount == 0, rows);
	}
}
