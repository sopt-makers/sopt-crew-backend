package org.sopt.makers.crew.main.notice.dto.request;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NoticeV2CreateRequestDto {
    private final String title;
    private final String subTitle;
    private final String contents;
    private final LocalDateTime exposeStartDate;
    private final LocalDateTime exposeEndDate;
    private final String noticeSecretKey;
}
