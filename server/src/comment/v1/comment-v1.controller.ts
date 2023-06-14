import {
  Body,
  Controller,
  Get,
  HttpStatus,
  Param,
  Post,
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
import { CommentV1GetCommentResponseDto } from './dto/get-comments/comment-v1-get-comments-response.dto';
import { CommentV1CreateCommentResponseDto } from './dto/create-comment/comment-v1-create-comment-response.dto';
import { GetUser } from 'src/common/decorator/get-user.decorator';
import { User } from 'src/entity/user/user.entity';
import { CommentV1CreateCommentBodyDto } from './dto/create-comment/comment-v1-create-comment-body.dto';
import { CommentV1GetCommentQueryDto } from './dto/get-comments/comment-v1-get-comments-query.dto';
import { CommentV1CreateCommentReportParamDto } from './dto/create-comment-report/report-v1-create-comment-report-param.dto';
import { CommentV1CreateCommentReportResponseDto } from './dto/create-comment-report/report-v1-create-comment-report-response.dto';
import { CommentV1SwitchCommentLikeParamDto } from 'src/comment/v1/dto/switch-comment-like/like-v1-switch-comment-like-body.dto';

@ApiTags('댓글/대댓글')
@Controller('comment/v1')
export class CommentV1Controller {
  constructor(private readonly commentV1Service: CommentV1Service) {}

  @ApiOperation({
    summary: '모임 게시글 댓글 리스트 조회',
  })
  @ApiResponse({
    status: HttpStatus.BAD_REQUEST,
    description: '"모임이 없습니다." or "권한이 없습니다."',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @Get()
  async getComments(
    @Query() query: CommentV1GetCommentQueryDto,
  ): Promise<CommentV1GetCommentResponseDto> {
    return {
      meta: {
        page: 1,
        take: 10,
        itemCount: 3,
        pageCount: 1,
        hasPreviousPage: false,
        hasNextPage: false,
      },
      comments: [
        {
          id: 1,
          contents: '댓글 내용',
          updatedDate: new Date(),
          likeCount: 10,
          isLiked: true,
          user: {
            id: 1,
            name: '닉네임',
            profileImage: 'https://picsum.photos/200/300',
          },
        },
        {
          id: 2,
          contents: '댓글 내용',
          updatedDate: new Date(),
          likeCount: 10,
          isLiked: true,
          user: {
            id: 1,
            name: '닉네임',
            profileImage: 'https://picsum.photos/200/300',
          },
        },
        {
          id: 3,
          contents: '댓글 내용',
          updatedDate: new Date(),
          likeCount: 10,
          isLiked: true,
          user: {
            id: 1,
            name: '닉네임',
            profileImage: 'https://picsum.photos/200/300',
          },
        },
      ],
    };
  }

  @ApiOperation({
    summary: '댓글 좋아요 토글',
  })
  @ApiResponse({
    status: HttpStatus.BAD_REQUEST,
    description: '',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Post('/:commentId/like')
  async switchCommentLike(
    @Param() body: CommentV1SwitchCommentLikeParamDto,
    @GetUser() user: User,
  ): Promise<null> {
    return null;
  }

  @ApiOperation({
    summary: '댓글 신고',
  })
  @ApiResponse({
    status: HttpStatus.BAD_REQUEST,
    description: '',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Post('/:commentId/report')
  async createCommentReport(
    @Param() param: CommentV1CreateCommentReportParamDto,
    @GetUser() user: User,
  ): Promise<CommentV1CreateCommentReportResponseDto> {
    return {
      reportId: 1,
    };
  }

  @ApiOperation({
    summary: '모임 게시글 댓글 작성',
  })
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
    return {
      commentId: 1,
    };
  }
}
