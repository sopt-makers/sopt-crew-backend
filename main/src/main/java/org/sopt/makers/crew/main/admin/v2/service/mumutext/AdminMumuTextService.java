package org.sopt.makers.crew.main.admin.v2.service.mumutext;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sopt.makers.crew.main.admin.v2.dto.mumutext.AdminMumuTextBulkPreviewResponse;
import org.sopt.makers.crew.main.admin.v2.dto.mumutext.AdminMumuTextBulkPreviewRow;
import org.sopt.makers.crew.main.admin.v2.dto.mumutext.AdminMumuTextResponse;
import org.sopt.makers.crew.main.admin.v2.dto.mumutext.AdminMumuTextStatus;
import org.sopt.makers.crew.main.admin.v2.dto.mumutext.AdminMumuTextSummary;
import org.sopt.makers.crew.main.admin.v2.dto.mumutext.AdminMumuTextUpsertRequest;
import org.sopt.makers.crew.main.entity.post.MumuText;
import org.sopt.makers.crew.main.entity.post.MumuTextRepository;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.util.Time;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMumuTextService {

	private static final DateTimeFormatter SPACE_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
		.appendPattern("yyyy-MM-dd HH:mm")
		.optionalStart()
		.appendPattern(":ss")
		.optionalStart()
		.appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
		.optionalEnd()
		.optionalEnd()
		.toFormatter();

	private final MumuTextRepository mumuTextRepository;
	private final Time time;

	public List<AdminMumuTextResponse> getMumuTexts() {
		LocalDateTime now = time.now();
		return mumuTextRepository.findAllByOrderByShowStartDateAscIdAsc().stream()
			.map(mumuText -> AdminMumuTextResponse.from(mumuText, now))
			.sorted(Comparator.comparingInt((AdminMumuTextResponse response) -> statusRank(response.status()))
				.thenComparing(AdminMumuTextResponse::showStartDate)
				.thenComparing(AdminMumuTextResponse::id))
			.toList();
	}

	public AdminMumuTextSummary getSummary(List<AdminMumuTextResponse> responses) {
		return AdminMumuTextSummary.from(responses);
	}

	@Transactional
	public AdminMumuTextResponse createMumuText(AdminMumuTextUpsertRequest request) {
		validateUpsertRequest(request);
		validateNoOverlap(null, request.showStartDate(), request.showEndDate());

		MumuText mumuText = MumuText.create(
			request.normalizedText(),
			request.normalizedCategory(),
			request.showStartDate(),
			request.showEndDate()
		);

		return AdminMumuTextResponse.from(mumuTextRepository.save(mumuText), time.now());
	}

	@Transactional
	public AdminMumuTextResponse updateMumuText(Long mumuTextId, AdminMumuTextUpsertRequest request) {
		validateUpsertRequest(request);

		MumuText mumuText = mumuTextRepository.findByIdOrThrow(mumuTextId);
		validateNoOverlap(mumuTextId, request.showStartDate(), request.showEndDate());

		mumuText.update(
			request.normalizedText(),
			request.normalizedCategory(),
			request.showStartDate(),
			request.showEndDate()
		);

		return AdminMumuTextResponse.from(mumuText, time.now());
	}

	@Transactional
	public void deleteMumuText(Long mumuTextId) {
		MumuText mumuText = mumuTextRepository.findByIdOrThrow(mumuTextId);
		mumuTextRepository.delete(mumuText);
	}

	public AdminMumuTextBulkPreviewResponse previewBulk(String csvText) {
		List<BulkRowCandidate> candidates = parseCsv(csvText);
		validateBulkOverlaps(candidates);
		return toPreviewResponse(candidates);
	}

	@Transactional
	public AdminMumuTextBulkPreviewResponse createBulk(String csvText) {
		List<BulkRowCandidate> candidates = parseCsv(csvText);
		validateBulkOverlaps(candidates);

		AdminMumuTextBulkPreviewResponse preview = toPreviewResponse(candidates);
		if (!preview.valid()) {
			throw new BadRequestException("CSV 검증에 실패했습니다. 검증 결과를 확인해주세요.");
		}

		List<MumuText> mumuTexts = candidates.stream()
			.map(candidate -> MumuText.create(
				candidate.text,
				candidate.category,
				candidate.showStartDate,
				candidate.showEndDate
			))
			.toList();
		mumuTextRepository.saveAll(mumuTexts);

		return preview;
	}

	private AdminMumuTextBulkPreviewResponse toPreviewResponse(List<BulkRowCandidate> candidates) {
		return AdminMumuTextBulkPreviewResponse.from(candidates.stream()
			.map(BulkRowCandidate::toPreviewRow)
			.toList());
	}

	private void validateUpsertRequest(AdminMumuTextUpsertRequest request) {
		if (!StringUtils.hasText(request.normalizedText())) {
			throw new BadRequestException("질문 문구는 비워둘 수 없습니다.");
		}
		if (!StringUtils.hasText(request.normalizedCategory())) {
			throw new BadRequestException("구분은 비워둘 수 없습니다.");
		}
		validateDateRange(request.showStartDate(), request.showEndDate());
	}

	private void validateDateRange(LocalDateTime showStartDate, LocalDateTime showEndDate) {
		if (showStartDate == null || showEndDate == null) {
			throw new BadRequestException("노출 시작일시와 종료일시는 필수입니다.");
		}
		if (!showStartDate.isBefore(showEndDate)) {
			throw new BadRequestException("노출 종료일시는 시작일시보다 이후여야 합니다.");
		}
	}

	private void validateNoOverlap(Long mumuTextId, LocalDateTime showStartDate, LocalDateTime showEndDate) {
		List<MumuText> overlappingMumuTexts = mumuTextId == null
			? mumuTextRepository.findOverlappingMumuTexts(showStartDate, showEndDate)
			: mumuTextRepository.findOverlappingMumuTextsExcludingId(mumuTextId, showStartDate, showEndDate);

		if (!overlappingMumuTexts.isEmpty()) {
			MumuText overlapped = overlappingMumuTexts.get(0);
			throw new BadRequestException(
				"기존 질문(#" + overlapped.getId() + ")의 노출 기간과 겹칩니다."
			);
		}
	}

	private List<BulkRowCandidate> parseCsv(String csvText) {
		if (!StringUtils.hasText(csvText)) {
			return List.of(BulkRowCandidate.invalid(1, null, null, null, null, "CSV 내용은 비워둘 수 없습니다."));
		}

		List<String[]> rows = readCsvRows(csvText);
		if (rows.isEmpty()) {
			return List.of(BulkRowCandidate.invalid(1, null, null, null, null, "CSV 헤더가 필요합니다."));
		}

		Map<String, Integer> headerIndex = createHeaderIndex(rows.get(0));
		List<String> missingHeaders = findMissingHeaders(headerIndex);
		if (!missingHeaders.isEmpty()) {
			return List.of(BulkRowCandidate.invalid(1, null, null, null, null,
				"CSV 헤더에 " + String.join(", ", missingHeaders) + " 컬럼이 필요합니다."));
		}

		List<BulkRowCandidate> candidates = new ArrayList<>();
		for (int i = 1; i < rows.size(); i++) {
			String[] row = rows.get(i);
			if (isBlankRow(row)) {
				continue;
			}
			candidates.add(parseDataRow(i + 1, row, headerIndex));
		}

		if (candidates.isEmpty()) {
			return List.of(BulkRowCandidate.invalid(2, null, null, null, null, "등록할 질문 행이 없습니다."));
		}
		return candidates;
	}

	private List<String[]> readCsvRows(String csvText) {
		try (CSVReader csvReader = new CSVReader(new StringReader(csvText))) {
			return csvReader.readAll();
		} catch (IOException | CsvException e) {
			throw new BadRequestException("CSV 파싱 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	private Map<String, Integer> createHeaderIndex(String[] headerRow) {
		Map<String, Integer> headerIndex = new HashMap<>();
		for (int i = 0; i < headerRow.length; i++) {
			headerIndex.put(stripBom(headerRow[i]).trim(), i);
		}
		return headerIndex;
	}

	private List<String> findMissingHeaders(Map<String, Integer> headerIndex) {
		List<String> missingHeaders = new ArrayList<>();
		if (!headerIndex.containsKey("startDate") && !headerIndex.containsKey("showStartDate")) {
			missingHeaders.add("startDate");
		}
		if (!headerIndex.containsKey("endDate") && !headerIndex.containsKey("showEndDate")) {
			missingHeaders.add("endDate");
		}
		if (!headerIndex.containsKey("category")) {
			missingHeaders.add("category");
		}
		if (!headerIndex.containsKey("text")) {
			missingHeaders.add("text");
		}
		return missingHeaders;
	}

	private BulkRowCandidate parseDataRow(int rowNumber, String[] row, Map<String, Integer> headerIndex) {
		String text = trim(getValue(row, headerIndex.get("text")));
		String category = trim(getValue(row, headerIndex.get("category")));
		String showStartDateText = trim(getValue(row, findDateColumnIndex(headerIndex, "startDate", "showStartDate")));
		String showEndDateText = trim(getValue(row, findDateColumnIndex(headerIndex, "endDate", "showEndDate")));

		List<String> messages = new ArrayList<>();
		if (!StringUtils.hasText(text)) {
			messages.add("질문 문구는 필수입니다.");
		}
		if (!StringUtils.hasText(category)) {
			messages.add("구분은 필수입니다.");
		}

		LocalDateTime showStartDate = parseDate(rowNumber, "startDate", showStartDateText, messages);
		LocalDateTime showEndDate = parseDate(rowNumber, "endDate", showEndDateText, messages);
		if (showStartDate != null && showEndDate != null && !showStartDate.isBefore(showEndDate)) {
			messages.add("노출 종료일시는 시작일시보다 이후여야 합니다.");
		}

		return BulkRowCandidate.of(rowNumber, text, category, showStartDate, showEndDate, messages);
	}

	private Integer findDateColumnIndex(Map<String, Integer> headerIndex, String firstName, String secondName) {
		return headerIndex.containsKey(firstName) ? headerIndex.get(firstName) : headerIndex.get(secondName);
	}

	private LocalDateTime parseDate(int rowNumber, String columnName, String rawValue, List<String> messages) {
		if (!StringUtils.hasText(rawValue)) {
			messages.add(columnName + " 값은 필수입니다.");
			return null;
		}

		try {
			return rawValue.contains("T")
				? LocalDateTime.parse(rawValue, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
				: LocalDateTime.parse(rawValue, SPACE_DATE_TIME_FORMATTER);
		} catch (DateTimeParseException e) {
			messages.add(rowNumber + "행 " + columnName + " 형식이 올바르지 않습니다. 예: 2026-09-07 00:00");
			return null;
		}
	}

	private void validateBulkOverlaps(List<BulkRowCandidate> candidates) {
		for (int i = 0; i < candidates.size(); i++) {
			BulkRowCandidate current = candidates.get(i);
			if (!current.hasValidDateRange()) {
				continue;
			}
			for (int j = i + 1; j < candidates.size(); j++) {
				BulkRowCandidate next = candidates.get(j);
				if (next.hasValidDateRange() && isOverlapped(current, next)) {
					current.messages.add("CSV 내 " + next.rowNumber + "행과 노출 기간이 겹칩니다.");
					next.messages.add("CSV 내 " + current.rowNumber + "행과 노출 기간이 겹칩니다.");
				}
			}

			List<MumuText> overlappedMumuTexts = mumuTextRepository.findOverlappingMumuTexts(
				current.showStartDate,
				current.showEndDate
			);
			overlappedMumuTexts.stream()
				.map(mumuText -> "기존 질문(#" + mumuText.getId() + ")의 노출 기간과 겹칩니다.")
				.forEach(current.messages::add);
		}
	}

	private boolean isOverlapped(BulkRowCandidate first, BulkRowCandidate second) {
		return first.showStartDate.isBefore(second.showEndDate)
			&& first.showEndDate.isAfter(second.showStartDate);
	}

	private String getValue(String[] row, Integer index) {
		if (index == null || index >= row.length) {
			return null;
		}
		return row[index];
	}

	private String trim(String value) {
		return value == null ? null : value.trim();
	}

	private String stripBom(String value) {
		if (value != null && value.startsWith("\uFEFF")) {
			return value.substring(1);
		}
		return value;
	}

	private boolean isBlankRow(String[] row) {
		return Arrays.stream(row)
			.noneMatch(StringUtils::hasText);
	}

	private int statusRank(AdminMumuTextStatus status) {
		return switch (status) {
			case ACTIVE -> 0;
			case SCHEDULED -> 1;
			case ENDED -> 2;
		};
	}

	private static class BulkRowCandidate {
		private final int rowNumber;
		private final String text;
		private final String category;
		private final LocalDateTime showStartDate;
		private final LocalDateTime showEndDate;
		private final List<String> messages;

		private BulkRowCandidate(int rowNumber, String text, String category, LocalDateTime showStartDate,
			LocalDateTime showEndDate, List<String> messages) {
			this.rowNumber = rowNumber;
			this.text = text;
			this.category = category;
			this.showStartDate = showStartDate;
			this.showEndDate = showEndDate;
			this.messages = messages;
		}

		private static BulkRowCandidate of(int rowNumber, String text, String category, LocalDateTime showStartDate,
			LocalDateTime showEndDate, List<String> messages) {
			return new BulkRowCandidate(rowNumber, text, category, showStartDate, showEndDate, messages);
		}

		private static BulkRowCandidate invalid(int rowNumber, String text, String category,
			LocalDateTime showStartDate, LocalDateTime showEndDate, String message) {
			return new BulkRowCandidate(rowNumber, text, category, showStartDate, showEndDate,
				new ArrayList<>(List.of(message)));
		}

		private boolean hasValidDateRange() {
			return showStartDate != null && showEndDate != null && showStartDate.isBefore(showEndDate);
		}

		private AdminMumuTextBulkPreviewRow toPreviewRow() {
			if (messages.isEmpty()) {
				return AdminMumuTextBulkPreviewRow.valid(rowNumber, text, category, showStartDate, showEndDate);
			}
			return AdminMumuTextBulkPreviewRow.invalid(rowNumber, text, category, showStartDate, showEndDate, messages);
		}
	}
}
