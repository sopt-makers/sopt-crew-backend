package org.sopt.makers.crew.main.internal.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.List;

import org.sopt.makers.crew.main.entity.post.PostRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;
import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.internal.dto.InternalPostGetAllResponseDto;
import org.sopt.makers.crew.main.internal.dto.InternalPostResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostDetailWithPartBaseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternalPostService {

	private final PostRepository postRepository;
	private final UserRepository userRepository;

	public InternalPostGetAllResponseDto getPosts(PageOptionsDto pageOptionsDto, Integer orgId) {

		Pageable pageRequest = PageRequest.of(pageOptionsDto.getPage() - 1, pageOptionsDto.getTake());

		User user = userRepository.findByOrgId(orgId)
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_USER.getErrorCode()));

		Page<PostDetailWithPartBaseDto> postList =
			postRepository.findPostList(pageRequest, user.getId());

		List<InternalPostResponseDto> list = postList.getContent()
			.stream()
			.map(InternalPostResponseDto::from)
			.toList();

		PageMetaDto pageMetaDto = new PageMetaDto(pageOptionsDto, (int)postList.getTotalElements());

		return InternalPostGetAllResponseDto.from(list, pageMetaDto);
	}
}
