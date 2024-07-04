package org.sopt.makers.crew.main.comment.v2.service;

import org.sopt.makers.crew.main.comment.v2.dto.request.CommentV2CreateCommentBodyDto;
import org.sopt.makers.crew.main.comment.v2.dto.request.CommentV2MentionUserInCommentRequestDto;
import org.sopt.makers.crew.main.comment.v2.dto.response.CommentV2CreateCommentResponseDto;
import org.sopt.makers.crew.main.comment.v2.dto.response.CommentV2ReportCommentResponseDto;
import org.sopt.makers.crew.main.common.exception.BadRequestException;
import org.sopt.makers.crew.main.common.exception.ForbiddenException;

public interface CommentV2Service {

    CommentV2CreateCommentResponseDto createComment(CommentV2CreateCommentBodyDto requestBody,
        Integer userId);

    CommentV2ReportCommentResponseDto reportComment(Integer commentId, Integer userId)
        throws BadRequestException;

    void deleteComment(Integer commentId, Integer userId) throws ForbiddenException;

    void mentionUserInComment(CommentV2MentionUserInCommentRequestDto requestBody, Integer userId);
}
