package org.sopt.makers.crew.main.entity.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserActivityVO {

	@Schema(description = "파트", example = "서버")
	@NotNull
	private String part;

	@Schema(description = "기수", example = "36")
	@NotNull
	private int generation;

	public UserActivityVO(String part, int generation) {
		this.part = part;
		this.generation = generation;
	}
}