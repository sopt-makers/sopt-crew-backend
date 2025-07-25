package org.sopt.makers.crew.main.internal;

import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.internal.dto.InternalPostCreateRequestDto;
import org.sopt.makers.crew.main.internal.dto.InternalPostCreateResponseDto;
import org.sopt.makers.crew.main.internal.dto.InternalPostGetAllResponseDto;
import org.sopt.makers.crew.main.internal.service.InternalPostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
	public ResponseEntity<InternalPostGetAllResponseDto> getPosts(
		@PathVariable Integer orgId,
		@ModelAttribute @Valid PageOptionsDto pageOptionsDto
	) {
		return ResponseEntity.ok(internalPostService.getPosts(pageOptionsDto, orgId));
	}

	@Override
	@PostMapping("{orgId}")
	public ResponseEntity<InternalPostCreateResponseDto> createPost(
		@PathVariable Integer orgId,
		@RequestBody @Valid InternalPostCreateRequestDto requestDto
	) {
		InternalPostCreateResponseDto response = internalPostService.createPost(requestDto, orgId);
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(response);
	}
}
