package org.sopt.makers.crew.main.admin.v2.service.mumutext;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.admin.v2.dto.mumutext.AdminMumuTextBulkPreviewResponse;
import org.sopt.makers.crew.main.admin.v2.dto.mumutext.AdminMumuTextUpsertRequest;
import org.sopt.makers.crew.main.entity.post.MumuText;
import org.sopt.makers.crew.main.entity.post.MumuTextRepository;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.util.Time;

@ExtendWith(MockitoExtension.class)
class AdminMumuTextServiceTest {

	@Mock
	private MumuTextRepository mumuTextRepository;

	@Mock
	private Time time;

	@InjectMocks
	private AdminMumuTextService adminMumuTextService;

	@Test
	@DisplayName("무무씨 질문 등록 시 기존 노출 기간과 겹치면 예외가 발생한다")
	void createMumuText_WhenOverlapped_ShouldThrowException() {
		AdminMumuTextUpsertRequest request = new AdminMumuTextUpsertRequest(
			"이번 주 질문",
			"번쩍",
			LocalDateTime.of(2026, 9, 7, 0, 0),
			LocalDateTime.of(2026, 9, 14, 0, 0)
		);
		when(mumuTextRepository.findOverlappingMumuTexts(request.showStartDate(), request.showEndDate()))
			.thenReturn(List.of(MumuText.create("기존 질문", "기존 구분", request.showStartDate(), request.showEndDate())));

		assertThatThrownBy(() -> adminMumuTextService.createMumuText(request))
			.isInstanceOf(BadRequestException.class)
			.hasMessageContaining("노출 기간과 겹칩니다");
		verify(mumuTextRepository, never()).save(any());
	}

	@Test
	@DisplayName("무무씨 질문 등록 시 종료일시와 다음 시작일시가 같으면 겹치지 않는다")
	void createMumuText_WhenAdjacentPeriod_ShouldSave() {
		AdminMumuTextUpsertRequest request = new AdminMumuTextUpsertRequest(
			"이번 주 질문",
			"1차 이벤트 / 솝커톤",
			LocalDateTime.of(2026, 9, 14, 0, 0),
			LocalDateTime.of(2026, 9, 21, 0, 0)
		);
		when(mumuTextRepository.findOverlappingMumuTexts(request.showStartDate(), request.showEndDate()))
			.thenReturn(List.of());
		when(mumuTextRepository.save(any(MumuText.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(time.now()).thenReturn(LocalDateTime.of(2026, 9, 1, 0, 0));

		adminMumuTextService.createMumuText(request);

		ArgumentCaptor<MumuText> captor = ArgumentCaptor.forClass(MumuText.class);
		verify(mumuTextRepository).save(captor.capture());
		assertThat(captor.getValue().getCategory()).isEqualTo("1차 이벤트 / 솝커톤");
		assertThat(captor.getValue().getText()).isEqualTo("이번 주 질문");
	}

	@Test
	@DisplayName("CSV 일괄 등록 미리보기는 필수 헤더와 구분을 포함한 정상 데이터를 검증한다")
	void previewBulk_WhenValidCsv_ShouldReturnValidResponse() {
		String csvText = """
			startDate,endDate,category,text
			2026-09-07 00:00,2026-09-14 00:00,번쩍,이번 주 새롭게 시도한 일은?
			2026-09-14 00:00,2026-09-21 00:00,2차 이벤트 / 스터디,함께해서 좋았던 순간은?
			""";
		when(mumuTextRepository.findOverlappingMumuTexts(any(LocalDateTime.class), any(LocalDateTime.class)))
			.thenReturn(List.of());

		AdminMumuTextBulkPreviewResponse response = adminMumuTextService.previewBulk(csvText);

		assertThat(response.valid()).isTrue();
		assertThat(response.totalCount()).isEqualTo(2);
		assertThat(response.validCount()).isEqualTo(2);
		assertThat(response.rows()).extracting("category")
			.containsExactly("번쩍", "2차 이벤트 / 스터디");
	}

	@Test
	@DisplayName("CSV 일괄 등록 미리보기는 CSV 내부 노출 기간이 겹치면 실패한다")
	void previewBulk_WhenCsvRowsOverlapped_ShouldReturnInvalidResponse() {
		String csvText = """
			startDate,endDate,category,text
			2026-09-07 00:00,2026-09-15 00:00,번쩍,이번 주 새롭게 시도한 일은?
			2026-09-14 00:00,2026-09-21 00:00,2차 이벤트 / 네트워킹데이,함께해서 좋았던 순간은?
			""";
		when(mumuTextRepository.findOverlappingMumuTexts(any(LocalDateTime.class), any(LocalDateTime.class)))
			.thenReturn(List.of());

		AdminMumuTextBulkPreviewResponse response = adminMumuTextService.previewBulk(csvText);

		assertThat(response.valid()).isFalse();
		assertThat(response.invalidCount()).isEqualTo(2);
		assertThat(response.rows()).allSatisfy(row ->
			assertThat(row.messages()).anyMatch(message -> message.contains("노출 기간이 겹칩니다"))
		);
	}
}
