package org.sopt.makers.crew.main.entity.user.vo;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import org.sopt.makers.crew.main.global.exception.ServerException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class UserActivityVO {

	@Schema(description = "파트", example = "서버")
	@NotNull
	private final String part;

	@Schema(description = "기수", example = "36")
	@NotNull
	private final int generation;

	@JsonCreator
	public UserActivityVO(
		@JsonProperty("part") String part,
		@JsonProperty("generation") int generation) {

		validateUserActivityVO(part, generation);
		this.part = part;
		this.generation = generation;
	}

	private void validateUserActivityVO(String part, int generation) {
		// Part validation: Ensure it's not null, empty, or contain invalid characters.
		if (part == null || part.trim().isEmpty()) {
			throw new ServerException(INTERNAL_SERVER_ERROR.getErrorCode() + part);
		}

		// Validate that part only contains letters (and optionally spaces)
		if (!part.matches("^[a-zA-Z가-힣\s]+$")) {
			throw new IllegalArgumentException(INTERNAL_SERVER_ERROR.getErrorCode() + part);
		}

		// Generation validation: Must be a positive integer
		if (generation <= 0) {
			throw new IllegalArgumentException(INTERNAL_SERVER_ERROR.getErrorCode() + generation);
		}
	}
}