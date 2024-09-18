package org.sopt.makers.crew.main.entity.post;

import static org.sopt.makers.crew.main.common.exception.ErrorStatus.NOT_FOUND_POST;
import static org.sopt.makers.crew.main.entity.comment.QComment.comment;
import static org.sopt.makers.crew.main.entity.like.QLike.like;
import static org.sopt.makers.crew.main.entity.meeting.QMeeting.meeting;
import static org.sopt.makers.crew.main.entity.post.QPost.post;
import static org.sopt.makers.crew.main.entity.user.QUser.user;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.crew.main.common.exception.BadRequestException;
import org.sopt.makers.crew.main.entity.user.QUser;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.external.playground.entity.member_block.QMemberBlock;
import org.sopt.makers.crew.main.post.v2.dto.query.PostGetPostsCommand;
import org.sopt.makers.crew.main.post.v2.dto.response.CommenterThumbnails;
import org.sopt.makers.crew.main.post.v2.dto.response.PostDetailBaseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostDetailResponseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.QPostDetailBaseDto;
import org.sopt.makers.crew.main.post.v2.dto.response.QPostMeetingDto;
import org.sopt.makers.crew.main.post.v2.dto.response.QPostWriterInfoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostSearchRepositoryImpl implements PostSearchRepository {
	private final JPAQueryFactory queryFactory;

	@Override
	public Page<PostDetailResponseDto> findPostList(PostGetPostsCommand queryCommand, Pageable pageable, User user) {
		Integer meetingId = queryCommand.getMeetingId().orElse(null);
		List<PostDetailResponseDto> content = getContentList(pageable, meetingId, user);

		JPAQuery<Long> countQuery = getCount(meetingId);

		return PageableExecutionUtils.getPage(content,
			PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), countQuery::fetchFirst);
	}

	@Override
	public PostDetailBaseDto findPost(Integer userId, Integer postId) {
		PostDetailBaseDto postDetail = queryFactory
			.select(new QPostDetailBaseDto(
				post.id, post.title, post.contents, post.createdDate, post.images,
				new QPostWriterInfoDto(post.user.id, post.user.orgId, post.user.name, post.user.profileImage),
				post.likeCount,
				ExpressionUtils.as(
					JPAExpressions.selectFrom(like)
						.where(like.postId.eq(post.id).and(like.userId.eq(userId)))
						.exists()
					, "isLiked"
				),
				post.viewCount, post.commentCount,
				new QPostMeetingDto(post.meeting.id, post.meeting.title, post.meeting.category, post.meeting.imageURL,
					post.meeting.desc)
			))
			.from(post)
			.innerJoin(post.meeting, meeting)
			.innerJoin(post.user, user)
			.where(post.id.eq(postId))
			.fetchFirst();

		if (postDetail == null) {
			throw new BadRequestException(NOT_FOUND_POST.getErrorCode());
		}

		return postDetail;
	}

	private List<PostDetailResponseDto> getContentList(Pageable pageable, Integer meetingId, User user) {
		List<PostDetailBaseDto> postDetails = queryFactory
				.select(
						new QPostDetailBaseDto(
								post.id,
								post.title,
								post.contents,
								post.createdDate,
								post.images,
								new QPostWriterInfoDto(post.user.id, post.user.orgId, post.user.name, post.user.profileImage),
								post.likeCount,
								ExpressionUtils.as(
										JPAExpressions.selectFrom(like)
												.where(like.postId.eq(post.id).and(like.userId.eq(user.getId())))
												.exists(), "isLiked"
								),
								post.viewCount,
								post.commentCount,
								new QPostMeetingDto(post.meeting.id, post.meeting.title, post.meeting.category, post.meeting.imageURL, post.meeting.desc)
						)
				)
				.from(post)
				.innerJoin(post.user, QUser.user)
				.innerJoin(post.meeting, meeting)
				.where(meetingIdEq(meetingId))
				.orderBy(post.id.desc())
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.fetch();

		// 모든 게시글 ID를 추출
		List<Integer> postIds = postDetails.stream()
				.map(PostDetailBaseDto::getId)
				.collect(Collectors.toList());

		// 차단된 유저의 게시물 여부를 확인하는 서브쿼리
		Map<Integer, Boolean> blockedPostMap = postIds.stream()
				.collect(Collectors.toMap(
						id -> id,
						id -> queryFactory
								.select(JPAExpressions.selectOne()
										.from(QMemberBlock.memberBlock)
										.where(QMemberBlock.memberBlock.blocker.eq((user.getOrgId().longValue()))
												.and(QMemberBlock.memberBlock.blockedMember.eq(post.user.orgId.longValue()))
												.and(QMemberBlock.memberBlock.isBlocked.isTrue()))
										.exists())
								.from(post)
								.where(post.id.eq(id))
								.fetchFirst()
				));

		// 게시글 ID 리스트를 사용하여 모든 댓글 작성자의 프로필 이미지를 조회
		Map<Integer, List<String>> commenterThumbnailsMap = queryFactory
				.select(comment.post.id, comment.user.profileImage)
				.from(comment)
				.where(comment.post.id.in(postIds))
				.groupBy(comment.post.id, comment.user.id, comment.user.profileImage)
				.orderBy(comment.post.id.asc(), comment.user.id.asc())
				.limit(3)
				.transform(GroupBy.groupBy(comment.post.id).as(GroupBy.list(comment.user.profileImage)));

		// 각 게시글에 차단 여부와 댓글 작성자 정보를 추가하여 반환
		List<PostDetailResponseDto> responseDtos = new ArrayList<>();
		for (PostDetailBaseDto postDetail : postDetails) {
			List<String> commenterThumbnails = commenterThumbnailsMap.getOrDefault(postDetail.getId(), Collections.emptyList());
			boolean isBlockedPost = blockedPostMap.getOrDefault(postDetail.getId(), false);
			responseDtos.add(PostDetailResponseDto.of(postDetail, new CommenterThumbnails(commenterThumbnails), isBlockedPost));
		}

		return responseDtos;
	}

	private JPAQuery<Long> getCount(Integer meetingId) {
		return queryFactory
			.select(post.count())
			.from(post)
			.where(meetingIdEq(meetingId));
	}

	private BooleanExpression meetingIdEq(Integer meetingId) {
		return meetingId == null ? null : post.meetingId.eq(meetingId);
	}

}
