import {
  Body,
  Controller,
  Get,
  HttpCode,
  HttpStatus,
  Param,
  ParseIntPipe,
  Post,
  Put,
  Query,
  UseGuards,
} from '@nestjs/common';
import { MeetingV1Service } from './meeting-v1.service';
import { User } from 'src/entity/user/user.entity';
import { AuthGuard } from '@nestjs/passport';
import {
  ApiTags,
  ApiOperation,
  ApiBearerAuth,
  ApiResponse,
  getSchemaPath,
} from '@nestjs/swagger';
import { BaseExceptionDto } from 'src/common/dto/base-exception.dto';
import { GetUser } from 'src/common/decorator/get-user.decorator';
import { MeetingV1UpdateMeetingBodyDto } from './dto/meeting-v1-update-meeting-body.dto';
import { MeetingV1GetPresignedUrlResponseDto } from './dto/get-presigned-url/meeting-v1-get-presigned-url-response.dto';
import { MeetingV1GetPresignedUrlQueryDto } from './dto/get-presigned-url/meeting-v1-get-presigned-url-query.dto';
import { MeetingV1CreateMeetingResponseDto } from './dto/create-meeting/meeting-v1-create-meeting-response.dto';
import { MeetingV1CreateMeetingBodyDto } from './dto/create-meeting/meeting-v1-create-meeting-body.dto';

@ApiTags('모임')
@Controller('meeting/v1')
export class MeetingV1Controller {
  constructor(private meetingV1Service: MeetingV1Service) {}

  @ApiOperation({
    summary: 'Meeting 썸네일 업로드용 Pre-Signed URL 발급',
  })
  @ApiResponse({
    status: HttpStatus.OK,
    description: '발급 완료',
    type: MeetingV1GetPresignedUrlResponseDto,
  })
  @ApiResponse({
    status: HttpStatus.INTERNAL_SERVER_ERROR,
    description: '발급 실패',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @ApiBearerAuth()
  @Get('/presigned-url')
  @UseGuards(AuthGuard('jwt'))
  async getPresignedURL(
    @Query() query: MeetingV1GetPresignedUrlQueryDto,
  ): Promise<MeetingV1GetPresignedUrlResponseDto> {
    return this.meetingV1Service.getPresignedUrl(query.contentType);
  }

  @ApiOperation({
    summary: '모임 생성',
    description: '모임 생성',
  })
  @ApiResponse({
    status: HttpStatus.CREATED,
    description: '생성완료',
    type: MeetingV1CreateMeetingResponseDto,
  })
  @ApiResponse({
    status: HttpStatus.BAD_REQUEST,
    description:
      '"이미지 파일이 없습니다." or "한 개 이상의 파트를 입력해주세요" or "프로필을 입력해주세요"',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @ApiBearerAuth()
  @Post('/')
  @UseGuards(AuthGuard('jwt'))
  async createMeeting(
    @Body() body: MeetingV1CreateMeetingBodyDto,
    @GetUser() user: User,
  ): Promise<MeetingV1CreateMeetingResponseDto> {
    return this.meetingV1Service.createMeeting(body, user);
  }

  @ApiOperation({
    summary: '모임 수정',
    description: '모임 수정',
  })
  @ApiResponse({
    status: HttpStatus.NO_CONTENT,
    description: '수정완료',
  })
  @ApiResponse({
    status: HttpStatus.BAD_REQUEST,
    description:
      '"이미지 파일이 없습니다." or "한 개 이상의 파트를 입력해주세요" or "조건에 맞는 모임이 없습니다."',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @HttpCode(HttpStatus.NO_CONTENT)
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Put('/:id')
  async updateMeetingById(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: MeetingV1UpdateMeetingBodyDto,
    @GetUser() user: User,
  ) {
    return this.meetingV1Service.updateMeetingById(id, body, user);
  }
}
