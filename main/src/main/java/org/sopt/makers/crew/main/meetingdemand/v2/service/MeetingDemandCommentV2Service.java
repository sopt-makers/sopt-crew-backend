package org.sopt.makers.crew.main.meetingdemand.v2.service;

import org.sopt.makers.crew.main.meetingdemand.v2.dto.query.MeetingDemandCommentV2GetCommentsQueryDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.request.MeetingDemandCommentV2CreateCommentBodyDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.request.MeetingDemandCommentV2MentionUserInCommentRequestDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandCommentV2CreateCommentResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandCommentV2GetCommentsResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandCommentV2SwitchCommentLikeResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandCommentV2UpdateCommentResponseDto;

public interface MeetingDemandCommentV2Service {

	MeetingDemandCommentV2GetCommentsResponseDto getComments(Integer meetingDemandId,
		MeetingDemandCommentV2GetCommentsQueryDto queryDto, Integer userId);

	MeetingDemandCommentV2CreateCommentResponseDto createComment(Integer meetingDemandId,
		MeetingDemandCommentV2CreateCommentBodyDto requestBody, Integer userId);

	MeetingDemandCommentV2UpdateCommentResponseDto updateComment(Integer commentId, String contents, Integer userId);

	void deleteComment(Integer commentId, Integer userId);

	MeetingDemandCommentV2SwitchCommentLikeResponseDto switchCommentLike(Integer commentId, Integer userId);

	void mentionUserInComment(MeetingDemandCommentV2MentionUserInCommentRequestDto requestBody, Integer userId);
}
