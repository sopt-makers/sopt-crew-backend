package org.sopt.makers.crew.main.post.v2.dto.response;

import com.querydsl.core.annotations.QueryProjection;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(name = "PostWriterInfoDto", description = "게시글 작성자 Dto")
public class PostWriterInfoDto {

    @Schema(description = "유저 id", example = "1")
    @NotNull
    private final Integer id;

    @Schema(description = "유저 org id", example = "1")
    @NotNull
    private final Integer orgId;

    @Schema(description = "유저 이름", example = "홍길동")
    @NotNull
    private final String name;

    @Schema(description = "유저 프로필 사진", example = "[url] 형식")
    @NotNull
    private final String profileImage;

    @QueryProjection
    public PostWriterInfoDto(Integer id, Integer orgId, String name, String profileImage) {
        this.id = id;
        this.orgId = orgId;
        this.name = name;
        this.profileImage = profileImage;
    }
}
