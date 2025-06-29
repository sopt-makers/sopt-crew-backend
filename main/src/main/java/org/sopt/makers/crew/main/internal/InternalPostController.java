package org.sopt.makers.crew.main.internal;

import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.internal.dto.InternalPostGetAllResponseDto;
import org.sopt.makers.crew.main.internal.service.InternalPostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/post")
@RequiredArgsConstructor
public class InternalPostController implements InternalPostApi {

	private final InternalPostService internalPostService;

	@Override
	@GetMapping("/{orgId}")
	public ResponseEntity<InternalPostGetAllResponseDto> getPosts(@PathVariable Integer orgId,
		@ModelAttribute @Valid PageOptionsDto pageOptionsDto) {
		return ResponseEntity.ok(internalPostService.getPosts(pageOptionsDto, orgId));
	}
}
