import {
  Body,
  Controller,
  Delete,
  Get,
  HttpStatus,
  Param,
  ParseIntPipe,
  Post,
  Put,
  Query,
  UploadedFiles,
  UseGuards,
  UseInterceptors,
} from '@nestjs/common';
import { MeetingV0Service } from './meeting-v0.service';
import { MeetingV0CreateMeetingDto } from './dto/meeting-v0-create-meeting.dto';
import { FilesInterceptor } from '@nestjs/platform-express/multer/interceptors/files.interceptor';
import { MeetingV0UpdateMeetingDto } from './dto/meeting-v0-update-meeting.dto';
import { User } from 'src/entity/user/user.entity';
import { AuthGuard } from '@nestjs/passport';
import {
  ApiTags,
  ApiOperation,
  ApiConsumes,
  ApiBearerAuth,
  ApiParam,
  ApiResponse,
  getSchemaPath,
} from '@nestjs/swagger';
import { MeetingV0ApplyMeetingDto } from './dto/meeting-v0-apply-meeting.dto';
import { MeetingV0GetMeetingDto } from './dto/meeting-v0-get-meeting.dto';
import { MeetingV0GetListDto } from './dto/meeting-v0-get-list.dto';
import { MeetingV0UpdateStatusApplyDto } from './dto/meeting-v0-update-status-apply.dto';
import { BaseExceptionDto } from 'src/common/dto/base-exception.dto';
import { MeetingV0GetMeetingByIdResponseDto } from './dto/meeting-v0-get-meeting-by-id-response.dto';
import { MeetingV0GetApplyListByMeetingResponseDto } from './dto/get-apply-list-by-meeting/meeting-v0-get-apply-list-by-meeting-response.dto';
import { MeetingV0GetAllMeetingsResponseDto } from './dto/get-all-meetings/meeting-v0-get-all-meetings-response.dto';
import { GetUser } from 'src/common/decorator/get-user.decorator';
import { Meeting } from '../../entity/meeting/meeting.entity';

@ApiTags('모임')
@Controller('meeting')
export class MeetingV0Controller {
  constructor(private meetingV0Service: MeetingV0Service) {}

  @ApiOperation({
    summary: '모임 지원자 상태 변경',
    description: '모임 지원자 상태 변경',
  })
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Put('/:id/apply/status')
  @ApiParam({ name: 'id', required: true, description: '모임 id' })
  async updateApplyStatusByMeeting(
    @Param('id', ParseIntPipe) id: number,
    @Body() updateStatusApplyDto: MeetingV0UpdateStatusApplyDto,
    @GetUser() user: User,
  ) {
    return this.meetingV0Service.updateApplyStatusByMeeting(
      id,
      user,
      updateStatusApplyDto,
    );
  }

  @ApiOperation({
    summary: '모임 지원자/참여자 조회',
    description:
      '모임 지원자/참여자 조회 (모임장이면 지원자, 아니면 참여자 조회)',
  })
  @ApiResponse({
    status: HttpStatus.BAD_REQUEST,
    description: '모임이 없습니다',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Get('/:id/list')
  @ApiParam({ name: 'id', required: true, description: '모임 id' })
  async getApplyListByMeeting(
    @Query() getListDto: MeetingV0GetListDto,
    @Param('id', ParseIntPipe) id: number,
    @GetUser() user: User,
  ): Promise<MeetingV0GetApplyListByMeetingResponseDto> {
    return this.meetingV0Service.getApplyListByMeeting(id, user, getListDto);
  }

  @ApiOperation({
    summary: '모임 지원/취소',
    description: '모임 지원/취소',
  })
  @ApiResponse({
    status: HttpStatus.CREATED,
    description: '지원/취소 완료',
  })
  @ApiResponse({
    status: HttpStatus.BAD_REQUEST,
    description:
      '"모임이 없습니다" or "기수/파트를 설정해주세요" or "정원이 꽉찼습니다" or "활동 기수가 아닙니다." or "지원가능 파트가 아닙니다." or "32기 스터디는 23:00부터 신청할 수 있어요."',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Post('/apply')
  async applyMeeting(
    @Body() applyMeetingDto: MeetingV0ApplyMeetingDto,
    @GetUser() user: User,
  ) {
    return this.meetingV0Service.applyMeeting(applyMeetingDto, user);
  }

  @ApiOperation({
    summary: '모임 상세 조회',
    description: '모임 상세 조회',
  })
  @ApiResponse({
    status: HttpStatus.OK,
    description: '노출할 정보가 있는 경우',
    schema: { $ref: getSchemaPath(Meeting) },
  })
  @ApiResponse({
    status: HttpStatus.BAD_REQUEST,
    description: '모임이 없습니다',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @ApiBearerAuth()
  @ApiParam({ name: 'id', required: true, description: '모임 id' })
  @UseGuards(AuthGuard('jwt'))
  @Get('/:id')
  async getMeetingById(
    @Param('id', ParseIntPipe) id: number,
    @GetUser() user: User,
  ): Promise<MeetingV0GetMeetingByIdResponseDto> {
    return this.meetingV0Service.getMeetingById(id, user);
  }

  @ApiOperation({
    summary: '모임 전체 조회/검색/필터링',
    description: '모임 전체 조회/검색/필터링',
  })
  @Get('/')
  async getAllMeeting(
    @Query() getMeetingDto: MeetingV0GetMeetingDto,
  ): Promise<MeetingV0GetAllMeetingsResponseDto> {
    return this.meetingV0Service.getAllMeeting(getMeetingDto);
  }

  @ApiOperation({
    summary: '모임 삭제',
    description: '모임 삭제',
  })
  @ApiResponse({
    status: HttpStatus.OK,
    description: '삭제완료',
  })
  @ApiResponse({
    status: HttpStatus.BAD_REQUEST,
    description: '조건에 맞는 모임이 없습니다.',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @ApiParam({ name: 'id', required: true, description: '모임 id' })
  @Delete('/:id')
  async deleteMeetingById(
    @Param('id', ParseIntPipe) id: number,
  ): Promise<void> {
    return this.meetingV0Service.deleteMeetingById(id);
  }

  /**
   * @author @donghunee
   * @deprecated v0.1.0
   */
  @ApiOperation({
    summary: '모임 생성',
    description: '모임 생성',
    deprecated: true,
  })
  @ApiConsumes('multipart/form-data')
  @ApiResponse({
    status: HttpStatus.CREATED,
    description: '생성완료',
  })
  @ApiResponse({
    status: HttpStatus.BAD_REQUEST,
    description:
      '"이미지 파일이 없습니다." or "한 개 이상의 파트를 입력해주세요" or "프로필을 입력해주세요"',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @Post('/')
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @UseInterceptors(FilesInterceptor('files', 6))
  async createMeeting(
    @UploadedFiles() files: Array<Express.MulterS3.File>,
    @Body() createMeetingDto: MeetingV0CreateMeetingDto,
    @GetUser() user: User,
  ) {
    return this.meetingV0Service.createMeeting(createMeetingDto, files, user);
  }

  /**
   * @author @donghunee
   * @deprecated v0.1.0
   */
  @ApiOperation({
    summary: '모임 수정',
    description: '모임 수정',
    deprecated: true,
  })
  @ApiConsumes('multipart/form-data')
  @ApiResponse({
    status: HttpStatus.OK,
    description: '수정완료',
  })
  @ApiResponse({
    status: HttpStatus.BAD_REQUEST,
    description:
      '"이미지 파일이 없습니다." or "한 개 이상의 파트를 입력해주세요" or "조건에 맞는 모임이 없습니다."',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Put('/:id')
  @UseInterceptors(FilesInterceptor('files', 6))
  async updateMeetingById(
    @Param('id', ParseIntPipe) id: number,
    @UploadedFiles() files: Array<Express.MulterS3.File>,
    @Body() updateMeetingDto: MeetingV0UpdateMeetingDto,
    @GetUser() user: User,
  ) {
    return this.meetingV0Service.updateMeetingById(
      id,
      updateMeetingDto,
      files,
      user,
    );
  }
}
