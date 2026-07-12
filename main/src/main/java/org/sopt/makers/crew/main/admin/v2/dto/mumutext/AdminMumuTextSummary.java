package org.sopt.makers.crew.main.admin.v2.dto.mumutext;

import java.util.List;

public record AdminMumuTextSummary(
	long activeCount,
	long scheduledCount,
	long endedCount,
	long validationRequiredCount
) {
	public static AdminMumuTextSummary from(List<AdminMumuTextResponse> responses) {
		return new AdminMumuTextSummary(
			countByStatus(responses, AdminMumuTextStatus.ACTIVE),
			countByStatus(responses, AdminMumuTextStatus.SCHEDULED),
			countByStatus(responses, AdminMumuTextStatus.ENDED),
			0
		);
	}

	private static long countByStatus(List<AdminMumuTextResponse> responses, AdminMumuTextStatus status) {
		return responses.stream()
			.filter(response -> response.status() == status)
			.count();
	}
}
