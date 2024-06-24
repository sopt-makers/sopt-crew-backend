package org.sopt.makers.crew.main.post.v2.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class PostWriterInfoDto {
    private final Integer id;
    private final Integer orgId;
    private final String name;
    private final String profileImage;

    @QueryProjection
    public PostWriterInfoDto(Integer id, Integer orgId, String name, String profileImage) {
        this.id = id;
        this.orgId = orgId;
        this.name = name;
        this.profileImage = profileImage;
    }
}
