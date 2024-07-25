package org.sopt.makers.crew.main.notice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.sopt.makers.crew.main.notice.dto.request.NoticeV2CreateRequestDto;
import org.sopt.makers.crew.main.notice.dto.response.NoticeV2GetResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "공지사항")
public interface NoticeV2Api {

    @Operation(summary = "공지사항 조회")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "성공")})
    ResponseEntity<List<NoticeV2GetResponseDto>> getNotices();

    @Operation(summary = "공지사항 작성")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "성공")})
    ResponseEntity<Void> createNotice(@RequestBody NoticeV2CreateRequestDto requestDto);
}
