package org.sopt.makers.crew.main.internal.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.like.Like;
import org.sopt.makers.crew.main.entity.like.LikeRepository;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.post.Post;
import org.sopt.makers.crew.main.entity.post.PostRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.sopt.makers.crew.main.internal.dto.ApprovedStudyCountResponseDto;
import org.sopt.makers.crew.main.internal.dto.InternalMeetingCountResponseDto;
import org.sopt.makers.crew.main.internal.dto.InternalPostLikeRequestDto;
import org.sopt.makers.crew.main.internal.dto.InternalPostLikeResponseDto;
import org.sopt.makers.crew.main.internal.dto.TopFastestAppliedMeetingResponseDto;
import org.sopt.makers.crew.main.internal.dto.TopFastestAppliedMeetingsResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternalMeetingStatsService {
	private final ApplyRepository applyRepository;
	private final UserRepository userRepository;
	private final MeetingRepository meetingRepository;
	private final PostRepository postRepository;
	private final LikeRepository likeRepository;

	public ApprovedStudyCountResponseDto getApprovedStudyCountByOrgId(Integer orgId) {
		User user = userRepository.findById(orgId).orElse(null);

		if (user == null) {
			return ApprovedStudyCountResponseDto.of(orgId, 0L);
		}

		Long approvedStudyCount = applyRepository.findApprovedStudyCountByOrgId(MeetingCategory.STUDY,
			EnApplyStatus.APPROVE, user.getId());

		return ApprovedStudyCountResponseDto.of(user.getId(), approvedStudyCount);
	}

	public TopFastestAppliedMeetingsResponseDto getTopFastestAppliedMeetings(Integer orgId, Integer queryCount,
		Integer queryYear) {
		Optional<User> user = userRepository.findById(orgId);

		if (user.isEmpty()) {
			return TopFastestAppliedMeetingsResponseDto.from(Collections.emptyList());
		}

		List<Apply> applies = applyRepository.findTopFastestAppliedMeetings(user.get().getId(), queryCount, queryYear);

		List<TopFastestAppliedMeetingResponseDto> responseDtos = applies.stream()
			.map(apply -> TopFastestAppliedMeetingResponseDto.from(apply.getMeeting()))
			.toList();

		return TopFastestAppliedMeetingsResponseDto.from(responseDtos);
	}

	public InternalMeetingCountResponseDto getMeetingCountByGeneration(Integer generation) {
		Integer meetingCount = meetingRepository.countAllByCreatedGeneration(generation);

		return InternalMeetingCountResponseDto.from(meetingCount);
	}

	@Transactional
	public InternalPostLikeResponseDto switchPostLike(InternalPostLikeRequestDto requestDto) {
		Integer postId = requestDto.postId();
		Integer orgId = requestDto.orgId();
		User user = userRepository.findById(orgId)
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_USER.getErrorCode()));
		Post post = postRepository.findByIdOrThrow(postId);
		Integer userId = user.getId();

		Optional<Like> existingLike = likeRepository.findByUserIdAndPostId(userId, postId);

		if (existingLike.isPresent()) {
			likeRepository.delete(existingLike.get());
			post.decreaseLikeCount();
			return InternalPostLikeResponseDto.from(false);
		}

		Like newLike = Like.builder()
			.postId(postId)
			.userId(userId)
			.build();

		likeRepository.save(newLike);
		post.increaseLikeCount();
		return InternalPostLikeResponseDto.from(true);
	}
}
