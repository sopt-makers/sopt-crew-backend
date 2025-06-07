package org.sopt.makers.crew.main.user.v2.dto.response;

import java.util.List;

public record UserV2GetInterestedKeywordsResponseDto(
	List<String> keywords
) {
	public static UserV2GetInterestedKeywordsResponseDto from(List<String> keywords) {
		return new UserV2GetInterestedKeywordsResponseDto(keywords);
	}
}
