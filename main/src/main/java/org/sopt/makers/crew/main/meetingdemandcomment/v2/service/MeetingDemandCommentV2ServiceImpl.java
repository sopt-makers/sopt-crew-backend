package org.sopt.makers.crew.main.meetingdemandcomment.v2.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.ALREADY_REPORTED_MEETING_DEMAND_COMMENT;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.FORBIDDEN_EXCEPTION;

import java.util.List;

import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemand;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandComment;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandCommentLike;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandCommentLikeRepository;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandCommentProfile;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandCommentRepository;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandRepository;
import org.sopt.makers.crew.main.entity.report.Report;
import org.sopt.makers.crew.main.entity.report.ReportRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.ForbiddenException;
import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;
import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.global.util.MentionSecretStringRemover;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.meetingdemand.v2.service.MeetingDemandPageNormalizer;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.query.MeetingDemandCommentV2GetCommentsQueryDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.request.MeetingDemandCommentV2CreateCommentBodyDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.request.MeetingDemandCommentV2MentionUserInCommentRequestDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandCommentDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandCommentV2CreateCommentResponseDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandCommentV2GetCommentsResponseDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandCommentV2ReportCommentResponseDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandCommentV2SwitchCommentLikeResponseDto;
import org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response.MeetingDemandCommentV2UpdateCommentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingDemandCommentV2ServiceImpl implements MeetingDemandCommentV2Service {

	private static final int PARENT_COMMENT = 0;
	private static final int REPLY_COMMENT = 1;

	private final MeetingDemandRepository meetingDemandRepository;
	private final MeetingDemandCommentRepository meetingDemandCommentRepository;
	private final MeetingDemandCommentLikeRepository meetingDemandCommentLikeRepository;
	private final ReportRepository reportRepository;
	private final UserRepository userRepository;
	private final MeetingDemandPageNormalizer meetingDemandPageNormalizer;
	private final MeetingDemandCommentFactory meetingDemandCommentFactory;
	private final MeetingDemandCommentProfileFactory meetingDemandCommentProfileFactory;
	private final MeetingDemandCommentResponseFactory meetingDemandCommentResponseFactory;
	private final MeetingDemandCommentNotificationSender meetingDemandCommentNotificationSender;
	private final Time time;

	@Override
	public MeetingDemandCommentV2GetCommentsResponseDto getComments(Integer meetingDemandId,
		MeetingDemandCommentV2GetCommentsQueryDto queryDto, Integer userId) {
		meetingDemandRepository.findByIdOrThrow(meetingDemandId);

		int totalCount = meetingDemandCommentRepository.countByMeetingDemandIdAndDepth(meetingDemandId, PARENT_COMMENT);
		PageOptionsDto effectiveQueryDto = meetingDemandPageNormalizer.normalize(queryDto, totalCount);
		Page<MeetingDemandComment> parentComments = meetingDemandCommentRepository.findAllByMeetingDemandIdAndDepth(
			meetingDemandId,
			PARENT_COMMENT,
			PageRequest.of(effectiveQueryDto.getPage() - 1, effectiveQueryDto.getTake(),
				Sort.by(Sort.Order.asc("createdTimestamp"), Sort.Order.asc("id")))
		);

		List<Integer> parentCommentIds = parentComments.getContent().stream()
			.map(MeetingDemandComment::getId)
			.toList();
		List<MeetingDemandComment> replyComments = getReplyComments(parentCommentIds);
		List<MeetingDemandCommentDto> commentDtos = meetingDemandCommentResponseFactory.createComments(
			meetingDemandId, parentComments.getContent(), replyComments, userId);

		PageMetaDto pageMetaDto = new PageMetaDto(effectiveQueryDto, totalCount);

		return MeetingDemandCommentV2GetCommentsResponseDto.of(commentDtos, pageMetaDto);
	}

	@Override
	@Transactional
	public MeetingDemandCommentV2CreateCommentResponseDto createComment(Integer meetingDemandId,
		MeetingDemandCommentV2CreateCommentBodyDto requestBody, Integer userId) {
		MeetingDemand meetingDemand = meetingDemandRepository.findByIdOrThrow(meetingDemandId);
		User writer = userRepository.findByIdOrThrow(userId);
		meetingDemandCommentProfileFactory.findOrCreate(meetingDemandId, userId);

		MeetingDemandCommentFactory.CreatedComment createdComment = meetingDemandCommentFactory.create(meetingDemand,
			writer, requestBody);

		MeetingDemandComment savedComment = meetingDemandCommentRepository.save(createdComment.comment());
		meetingDemand.increaseCommentCount();
		if (!meetingDemand.isWriter(userId)) {
			meetingDemandCommentNotificationSender.sendCommentNotification(meetingDemand, userId);
		}

		return MeetingDemandCommentV2CreateCommentResponseDto.of(savedComment.getId());
	}

	@Override
	@Transactional
	public MeetingDemandCommentV2UpdateCommentResponseDto updateComment(Integer commentId, String contents,
		Integer userId) {
		MeetingDemandComment comment = meetingDemandCommentRepository.findByIdOrThrow(commentId);

		comment.validateWriter(userId);
		comment.updateContents(contents);

		return MeetingDemandCommentV2UpdateCommentResponseDto.of(comment.getId(), comment.getContents(),
			String.valueOf(time.now()));
	}

	@Override
	@Transactional
	public void deleteComment(Integer commentId, Integer userId) {
		MeetingDemandComment comment = meetingDemandCommentRepository.findByIdOrThrow(commentId);
		comment.validateWriter(userId);

		MeetingDemand meetingDemand = meetingDemandRepository.findByIdOrThrow(comment.getMeetingDemandId());
		meetingDemand.decreaseCommentCount();

		List<MeetingDemandComment> childComments = meetingDemandCommentRepository
			.findAllByParentIdAndDepthOrderByOrderDesc(comment.getId(), REPLY_COMMENT);

		if (comment.isReplyComment() || childComments.isEmpty()) {
			meetingDemandCommentLikeRepository.deleteAllByMeetingDemandCommentId(commentId);
			meetingDemandCommentRepository.delete(comment);
			return;
		}

		MeetingDemandCommentProfile profile = meetingDemandCommentProfileFactory.findOrCreate(
			comment.getMeetingDemandId(), userId);
		comment.deleteParentComment();
		deleteMentionsInChildComments(childComments, profile.getAnonymousNickname(), userId);
	}

	@Override
	@Transactional
	public MeetingDemandCommentV2SwitchCommentLikeResponseDto switchCommentLike(Integer commentId, Integer userId) {
		MeetingDemandComment comment = meetingDemandCommentRepository.findByIdOrThrow(commentId);

		boolean isLiked = meetingDemandCommentLikeRepository.existsByMeetingDemandCommentIdAndUserId(commentId, userId);
		if (isLiked) {
			meetingDemandCommentLikeRepository.deleteByMeetingDemandCommentIdAndUserId(commentId, userId);
			comment.decreaseLikeCount();
			return MeetingDemandCommentV2SwitchCommentLikeResponseDto.of(false);
		}

		MeetingDemandCommentLike like = MeetingDemandCommentLike.builder()
			.meetingDemandCommentId(commentId)
			.userId(userId)
			.build();

		meetingDemandCommentLikeRepository.save(like);
		comment.increaseLikeCount();

		return MeetingDemandCommentV2SwitchCommentLikeResponseDto.of(true);
	}

	@Override
	@Transactional
	public void mentionUserInComment(MeetingDemandCommentV2MentionUserInCommentRequestDto requestBody, Integer userId) {
		meetingDemandRepository.findByIdOrThrow(requestBody.getMeetingDemandId());
		userRepository.findByIdOrThrow(userId);

		meetingDemandCommentNotificationSender.sendMentionNotification(requestBody);
	}

	@Override
	@Transactional
	public MeetingDemandCommentV2ReportCommentResponseDto reportComment(Integer commentId, Integer userId) {
		MeetingDemandComment comment = meetingDemandCommentRepository.findByIdOrThrow(commentId);

		if (comment.isWriter(userId)) {
			throw new ForbiddenException(FORBIDDEN_EXCEPTION.getErrorCode());
		}
		if (reportRepository.existsByMeetingDemandCommentIdAndUserId(commentId, userId)) {
			throw new BadRequestException(ALREADY_REPORTED_MEETING_DEMAND_COMMENT.getErrorCode());
		}

		Report report = Report.builder()
			.meetingDemandComment(comment)
			.meetingDemandCommentId(commentId)
			.userId(userId)
			.build();

		Report savedReport = reportRepository.save(report);

		return MeetingDemandCommentV2ReportCommentResponseDto.of(savedReport.getId());
	}

	private List<MeetingDemandComment> getReplyComments(List<Integer> parentCommentIds) {
		if (parentCommentIds.isEmpty()) {
			return List.of();
		}

		return meetingDemandCommentRepository.findAllByParentIdInAndDepthOrderByParentIdAscOrderAsc(parentCommentIds,
			REPLY_COMMENT);
	}

	private void deleteMentionsInChildComments(List<MeetingDemandComment> childComments, String mentionName,
		Integer mentionUserId) {
		childComments.forEach(comment -> {
			String deletedMentionContent = MentionSecretStringRemover.deleteMentionContent(comment.getContents(),
				mentionName, mentionUserId.toString());
			comment.updateContents(deletedMentionContent);
		});
	}
}
