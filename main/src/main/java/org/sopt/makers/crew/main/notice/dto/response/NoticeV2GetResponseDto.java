package org.sopt.makers.crew.main.notice.dto.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class NoticeV2GetResponseDto {
    private final Integer id;
    private final String title;
    private final String subTitle;
    private final String contents;
    private final LocalDateTime createdDate;
}
