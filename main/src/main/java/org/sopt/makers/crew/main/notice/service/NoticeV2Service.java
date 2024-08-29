package org.sopt.makers.crew.main.notice.service;

import static org.sopt.makers.crew.main.common.exception.ErrorStatus.FORBIDDEN_EXCEPTION;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.sopt.makers.crew.main.common.exception.ForbiddenException;
import org.sopt.makers.crew.main.common.util.Time;
import org.sopt.makers.crew.main.entity.notice.Notice;
import org.sopt.makers.crew.main.entity.notice.NoticeRepository;
import org.sopt.makers.crew.main.notice.dto.request.NoticeV2CreateRequestDto;
import org.sopt.makers.crew.main.notice.dto.response.NoticeV2GetResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeV2Service {
	private final NoticeRepository noticeRepository;

	private final Time time;

	@Value("${notice.secret-key}")
	private String noticeSecretKey;

	public List<NoticeV2GetResponseDto> getNotices() {
		// Time 클래스로 교체 필요.
		List<Notice> notices = noticeRepository.findByExposeStartDateBeforeAndExposeEndDateAfter(time.now(),
			time.now());
		return notices.stream()
			.map(notice -> NoticeV2GetResponseDto.of(notice.getId(), notice.getTitle(), notice.getSubTitle(),
				notice.getContents(),
				notice.getCreatedDate()))
			.toList();
	}

	@Transactional
	public void createNotice(NoticeV2CreateRequestDto requestDto) {

		if (!requestDto.getNoticeSecretKey().equals(noticeSecretKey)) {
			throw new ForbiddenException(FORBIDDEN_EXCEPTION.getErrorCode());
		}

		Notice notice = Notice.builder()
			.title(requestDto.getTitle())
			.subTitle(requestDto.getSubTitle())
			.contents(requestDto.getContents())
			.exposeStartDate(requestDto.getExposeStartDate())
			.exposeEndDate(requestDto.getExposeEndDate())
			.build();

		noticeRepository.save(notice);
	}
}
