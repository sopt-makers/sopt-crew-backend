package org.sopt.makers.crew.main.admin.v2;

import java.util.List;

import org.sopt.makers.crew.main.admin.v2.dto.mumutext.AdminMumuTextBulkPreviewResponse;
import org.sopt.makers.crew.main.admin.v2.dto.mumutext.AdminMumuTextBulkRequest;
import org.sopt.makers.crew.main.admin.v2.dto.mumutext.AdminMumuTextResponse;
import org.sopt.makers.crew.main.admin.v2.dto.mumutext.AdminMumuTextUpsertRequest;
import org.sopt.makers.crew.main.admin.v2.service.AdminKeyProvider;
import org.sopt.makers.crew.main.admin.v2.service.mumutext.AdminMumuTextService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/v2/${custom.paths.adminKey}/mumu-text")
@RequiredArgsConstructor
public class AdminMumuTextController {

	private final AdminMumuTextService adminMumuTextService;
	private final AdminKeyProvider adminKeyProvider;

	@GetMapping
	public ModelAndView mumuTextPage() {
		List<AdminMumuTextResponse> mumuTexts = adminMumuTextService.getMumuTexts();

		ModelAndView model = new ModelAndView("mumuTextPage");
		model.addObject("adminKey", adminKeyProvider.getAdminKey());
		model.addObject("mumuTexts", mumuTexts);
		model.addObject("summary", adminMumuTextService.getSummary(mumuTexts));
		return model;
	}

	@PostMapping
	public ResponseEntity<AdminMumuTextResponse> createMumuText(
		@Valid @RequestBody AdminMumuTextUpsertRequest request
	) {
		return ResponseEntity.ok(adminMumuTextService.createMumuText(request));
	}

	@PatchMapping("/{mumuTextId}")
	public ResponseEntity<AdminMumuTextResponse> updateMumuText(
		@PathVariable Long mumuTextId,
		@Valid @RequestBody AdminMumuTextUpsertRequest request
	) {
		return ResponseEntity.ok(adminMumuTextService.updateMumuText(mumuTextId, request));
	}

	@DeleteMapping("/{mumuTextId}")
	public ResponseEntity<Void> deleteMumuText(@PathVariable Long mumuTextId) {
		adminMumuTextService.deleteMumuText(mumuTextId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/bulk/preview")
	public ResponseEntity<AdminMumuTextBulkPreviewResponse> previewBulk(
		@Valid @RequestBody AdminMumuTextBulkRequest request
	) {
		return ResponseEntity.ok(adminMumuTextService.previewBulk(request.csvText()));
	}

	@PostMapping("/bulk")
	public ResponseEntity<AdminMumuTextBulkPreviewResponse> createBulk(
		@Valid @RequestBody AdminMumuTextBulkRequest request
	) {
		return ResponseEntity.ok(adminMumuTextService.createBulk(request.csvText()));
	}
}
