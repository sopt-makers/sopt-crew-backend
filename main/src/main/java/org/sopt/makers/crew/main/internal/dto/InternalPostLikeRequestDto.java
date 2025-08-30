package org.sopt.makers.crew.main.internal.dto;

import jakarta.validation.constraints.NotNull;

public record InternalPostLikeRequestDto(
	@NotNull
	Integer orgId,

	@NotNull
	Integer postId
) {
}
