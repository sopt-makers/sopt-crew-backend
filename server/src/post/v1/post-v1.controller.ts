import {
  Body,
  Controller,
  Get,
  HttpStatus,
  Param,
  ParseIntPipe,
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
import { PostV1Service } from './post-v1.service';
import { AuthGuard } from '@nestjs/passport';
import { BaseExceptionDto } from 'src/common/dto/base-exception.dto';
import { PostV1GetPostCountResponseDto } from 'src/post/v1/dto/get-meeting-post-count/post-v1-get-post-count-response.dto';
import { PostV1GetPostsResponseDto } from './dto/get-meeting-posts/post-v1-get-posts-response.dto';
import { PostV1GetPostCountQueryDto } from './dto/get-meeting-post-count/post-v1-get-post-count-query.dto';
import { PostV1GetPostsQueryDto } from './dto/get-meeting-posts/post-v1-get-posts-query.dto';
import { PostV1GetPostResponseDto } from './dto/get-meeting-post/post-v1-get-post-response.dto';
import { PostV1CreatePostBodyDto } from './dto/create-meeting-post/post-v1-create-post-body.dto';
import { GetUser } from 'src/common/decorator/get-user.decorator';
import { User } from 'src/entity/user/user.entity';
import { PostV1CreatePostResponseDto } from './dto/create-meeting-post/post-v1-create-post-response.dto';
import { PostV1ReportPostParamDto } from './dto/report-post/post-v1-report-post-param.dto';
import { PostV1ReportPostResponseDto } from './dto/report-post/post-v1-report-post-response.dto';
import { PostV1SwitchPostLikeParamDto } from './dto/switch-post-like/post-v1-switch-post-like-param.dto';
import { PostV1SwitchPostLikeResponseDto } from './dto/switch-post-like/post-v1-switch-post-like-response.dto';

@ApiTags('게시글')
@Controller('post/v1')
export class PostV1Controller {
  constructor(private readonly postV1Service: PostV1Service) {}

  @ApiOperation({
    summary: '모임 게시글 개수 조회',
  })
  @ApiResponse({
    status: HttpStatus.BAD_REQUEST,
    description: '"모임이 없습니다." or "권한이 없습니다."',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @Get('/count')
  async getPostCount(
    @Query() query: PostV1GetPostCountQueryDto,
  ): Promise<PostV1GetPostCountResponseDto> {
    return this.postV1Service.getPostCount(query);
  }

  @ApiOperation({
    summary: '모임 게시글 목록 조회',
  })
  @ApiResponse({
    status: HttpStatus.BAD_REQUEST,
    description: '"모임이 없습니다." or "권한이 없습니다."',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @Get()
  async getPosts(
    @Query() query: PostV1GetPostsQueryDto,
  ): Promise<PostV1GetPostsResponseDto> {
    return this.postV1Service.getPosts(query);
  }

  @ApiOperation({
    summary: '모임 게시글 조회',
  })
  @ApiResponse({
    status: HttpStatus.BAD_REQUEST,
    description: '"모임이 없습니다." or "권한이 없습니다."',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @Get('/:postId')
  async getMeetingPost(
    @Param('postId', ParseIntPipe) postId: number,
  ): Promise<PostV1GetPostResponseDto> {
    return this.postV1Service.getPost({ postId });
  }

  @ApiOperation({
    summary: '모임 게시글 작성',
  })
  @ApiResponse({
    status: HttpStatus.BAD_REQUEST,
    description: '모임이 없습니다.',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Post()
  async createPost(
    @Body() body: PostV1CreatePostBodyDto,
    @GetUser() user: User,
  ): Promise<PostV1CreatePostResponseDto> {
    return this.postV1Service.createPost({ body, user });
  }

  @ApiOperation({
    summary: '게시글 좋아요 토글',
  })
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Post('/:postId/like')
  async switchPostLike(
    @Param() param: PostV1SwitchPostLikeParamDto,
    @GetUser() user: User,
  ): Promise<PostV1SwitchPostLikeResponseDto> {
    return this.postV1Service.switchPostLike({ param, user });
  }

  @ApiOperation({
    summary: '모임 게시글 신고',
  })
  @ApiResponse({
    status: HttpStatus.BAD_REQUEST,
    description: '"모임이 없습니다." or "권한이 없습니다."',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Post('/:postId/report')
  async createPostReport(
    @Param() param: PostV1ReportPostParamDto,
    @GetUser() user: User,
  ): Promise<PostV1ReportPostResponseDto> {
    return this.postV1Service.reportPost({ param, user });
  }
}
