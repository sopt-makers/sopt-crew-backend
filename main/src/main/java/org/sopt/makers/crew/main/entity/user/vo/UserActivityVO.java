package org.sopt.makers.crew.main.entity.user.vo;

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

		this.part = part;
		this.generation = generation;
	}
}