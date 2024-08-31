package org.sopt.makers.crew.main.entity.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class UserActivityVO {

    @Schema(description = "파트", example = "서버")
    @NotNull
    private final String part;

    @Schema(description = "기수", example = "36")
    @NotNull
    private final int generation;

}