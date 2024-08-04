package org.sopt.makers.crew.main.notice;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.notice.dto.request.NoticeV2CreateRequestDto;
import org.sopt.makers.crew.main.notice.dto.response.NoticeV2GetResponseDto;
import org.sopt.makers.crew.main.notice.service.NoticeV2Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notice/v2")
@RequiredArgsConstructor
public class NoticeV2Controller implements NoticeV2Api {
    private final NoticeV2Service noticeService;


    @Override
    @GetMapping
    public ResponseEntity<List<NoticeV2GetResponseDto>> getNotices() {
        return ResponseEntity.ok(noticeService.getNotices());
    }

    @Override
    @PostMapping
    public ResponseEntity<Void> createNotice(@RequestBody NoticeV2CreateRequestDto requestDto) {
        noticeService.createNotice(requestDto);
        return ResponseEntity.ok(null);
    }

}
