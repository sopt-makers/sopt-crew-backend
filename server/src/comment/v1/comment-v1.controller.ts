import {
  Body,
  Controller,
  Delete,
  Get,
  HttpStatus,
  Param,
  Post,
  Put,
  Query,
  UseGuards,
} from '@nestjs/common';
import {
  ApiBearerAuth,
  ApiOperation,
  ApiResponse,
  ApiTags,
  getSchemaPath,
} from '@nestjs/swagger';
import { CommentV1Service } from './comment-v1.service';
import { AuthGuard } from '@nestjs/passport';
import { BaseExceptionDto } from 'src/common/dto/base-exception.dto';
import { CommentV1GetCommentsResponseDto } from './dto/get-comments/comment-v1-get-comments-response.dto';
import { CommentV1CreateCommentResponseDto } from './dto/create-comment/comment-v1-create-comment-response.dto';
import { GetUser } from 'src/common/decorator/get-user.decorator';
import { User } from 'src/entity/user/user.entity';
import { CommentV1CreateCommentBodyDto } from './dto/create-comment/comment-v1-create-comment-body.dto';
import { CommentV1GetCommentsQueryDto } from './dto/get-comments/comment-v1-get-comments-query.dto';
import { CommentV1ReportCommentParamDto } from './dto/report-comment/comment-v1-report-comment-param.dto';
import { CommentV1ReportCommentResponseDto } from './dto/report-comment/comment-v1-report-comment-response.dto';
import { CommentV1SwitchCommentLikeParamDto } from 'src/comment/v1/dto/switch-comment-like/comment-v1-switch-comment-like-param.dto';
import { CommentV1SwitchCommentLikeResponseDto } from './dto/switch-comment-like/comment-v1-switch-comment-like-response.dto';
import { ApiOkResponseCommon } from 'src/common/decorator/api-ok-response-common.decorator';
import { CommentV1DeleteCommentParamDto } from './dto/delete-comment/comment-v1-delete-comment-param.dto';
import { CommentV1UpdateCommentParamDto } from './dto/update-comment/comment-v1-update-comment-param.dto';
import { CommentV1UpdateCommentBodyDto } from './dto/update-comment/comment-v1-update-comment-body.dto';
import { CommentV1UpdateCommentResponseDto } from './dto/update-comment/comment-v1-update-comment-response.dto';

@ApiTags('댓글/대댓글')
@Controller('comment/v1')
export class CommentV1Controller {
  constructor(private readonly commentV1Service: CommentV1Service) {}

  @ApiOperation({
    summary: '모임 게시글 댓글 리스트 조회',
  })
  @ApiOkResponseCommon(CommentV1GetCommentsResponseDto)
  @ApiResponse({
    status: HttpStatus.BAD_REQUEST,
    description: '"모임이 없습니다." or "권한이 없습니다."',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Get()
  async getComments(
    @Query() query: CommentV1GetCommentsQueryDto,
    @GetUser() user: User,
  ): Promise<CommentV1GetCommentsResponseDto> {
    return this.commentV1Service.getComments({ query, user });
  }

  @ApiOperation({
    summary: '댓글 좋아요 토글',
  })
  @ApiOkResponseCommon(CommentV1SwitchCommentLikeResponseDto)
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Post('/:commentId/like')
  async switchCommentLike(
    @Param() param: CommentV1SwitchCommentLikeParamDto,
    @GetUser() user: User,
  ): Promise<CommentV1SwitchCommentLikeResponseDto> {
    return this.commentV1Service.switchCommentLike({ param, user });
  }

  @ApiOperation({
    summary: '댓글 신고',
  })
  @ApiOkResponseCommon(CommentV1ReportCommentResponseDto)
  @ApiResponse({
    status: HttpStatus.BAD_REQUEST,
    description: '',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Post('/:commentId/report')
  async reportComment(
    @Param() param: CommentV1ReportCommentParamDto,
    @GetUser() user: User,
  ): Promise<CommentV1ReportCommentResponseDto> {
    return this.commentV1Service.reportComment({ param, user });
  }

  @ApiOperation({
    summary: '모임 게시글 댓글 작성',
    deprecated: true,
  })
  @ApiOkResponseCommon(CommentV1CreateCommentResponseDto)
  @ApiResponse({
    status: HttpStatus.BAD_REQUEST,
    description: '',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Post()
  async createPostComment(
    @Body() body: CommentV1CreateCommentBodyDto,
    @GetUser() user: User,
  ): Promise<CommentV1CreateCommentResponseDto> {
    return this.commentV1Service.createPostComment({ body, user });
  }

  @ApiOperation({
    summary: '모임 게시글 댓글 수정',
  })
  @ApiOkResponseCommon(CommentV1CreateCommentResponseDto)
  @ApiResponse({
    status: HttpStatus.BAD_REQUEST,
    description: '"존재하지 않는 댓글입니다." or "권한이 없습니다."',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Put('/:commentId')
  async updatePostComment(
    @Param() param: CommentV1UpdateCommentParamDto,
    @Body() body: CommentV1UpdateCommentBodyDto,
    @GetUser() user: User,
  ): Promise<CommentV1UpdateCommentResponseDto> {
    return this.commentV1Service.updatePostComment({
      userId: user.id,
      commentId: param.commentId,
      body,
    });
  }

  /**
   * @deprecated
   */
  @ApiOperation({
    summary: '모임 게시글 댓글 삭제',
    deprecated: true,
  })
  @ApiResponse({
    status: HttpStatus.BAD_REQUEST,
    description: '',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Delete('/:commentId')
  async deletePostComment(
    @Param() param: CommentV1DeleteCommentParamDto,
    @GetUser() user: User,
  ): Promise<void> {
    return this.commentV1Service.deletePostComment({
      userId: user.id,
      commentId: param.commentId,
    });
  }
}
