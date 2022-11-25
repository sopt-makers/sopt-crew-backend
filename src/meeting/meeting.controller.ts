import {
  Body,
  Controller,
  Delete,
  Get,
  HttpCode,
  Param,
  ParseIntPipe,
  Post,
  Put,
  Query,
  UploadedFiles,
  UseGuards,
  UseInterceptors,
} from '@nestjs/common';
import { MeetingService } from './meeting.service';
import { CreateMeetingDto } from './dto/create-meeting.dto';
import { Meeting } from './meeting.entity';
import { FilesInterceptor } from '@nestjs/platform-express/multer/interceptors/files.interceptor';
import { UpdateMeetingDto } from './dto/update-metting-dto';
import { GetUser } from 'src/auth/get-user.decorator';
import { User } from 'src/users/user.entity';
import { AuthGuard } from '@nestjs/passport';
import {
  ApiTags,
  ApiOperation,
  ApiCreatedResponse,
  ApiSecurity,
  ApiExcludeEndpoint,
  ApiConsumes,
  ApiBody,
  ApiBearerAuth,
  ApiParam,
} from '@nestjs/swagger';
import { ApplyMeetingDto } from './dto/apply-meeting.dto';
import { GetMeetingDto } from './dto/get-meeting.dto';
import { GetListDto } from './dto/get-list.dto';
import { UpdateStatusApplyDto } from './dto/update-status-apply.dto';
import { PageOptionsDto } from 'src/pagination/dto/page-options.dto';

@ApiTags('모임')
@Controller('meeting')
export class MeetingController {
  constructor(private meetingService: MeetingService) {}

  @ApiOperation({
    summary: '모임 지원자 상태 변경',
    description: '모임 지원자 상태 변경',
  })
  @ApiBearerAuth('access-token')
  @UseGuards(AuthGuard('jwt'))
  @Put('/:id/apply/status')
  @ApiParam({ name: 'id', required: true, description: '모임 id' })
  updateApplyStatusByMeeting(
    @Param('id', ParseIntPipe) id: number,
    @Body() updateStatusApplyDto: UpdateStatusApplyDto,
    @GetUser() user: User,
  ) {
    return this.meetingService.updateApplyStatusByMeeting(
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
  @ApiBearerAuth('access-token')
  @UseGuards(AuthGuard('jwt'))
  @Get('/:id/list')
  @ApiParam({ name: 'id', required: true, description: '모임 id' })
  getListByMeeting(
    @Query() getListDto: GetListDto,
    @Param('id', ParseIntPipe) id: number,
    @GetUser() user: User,
  ) {
    return this.meetingService.getListByMeeting(id, user, getListDto);
  }

  @ApiOperation({
    summary: '모임 지원/취소',
    description: '모임 지원/취소',
  })
  @ApiBearerAuth('access-token')
  @UseGuards(AuthGuard('jwt'))
  @Post('/apply')
  applyMeeting(
    @Body() applyMeetingDto: ApplyMeetingDto,
    @GetUser() user: User,
  ) {
    return this.meetingService.applyMeeting(applyMeetingDto, user);
  }

  @ApiOperation({
    summary: '모임 상세 조회',
    description: '모임 상세 조회',
  })
  @ApiBearerAuth('access-token')
  @UseGuards(AuthGuard('jwt'))
  @ApiParam({ name: 'id', required: true, description: '모임 id' })
  @Get('/:id')
  getMeetingById(
    @Param('id', ParseIntPipe) id: number,
    @GetUser() user: User,
  ): Promise<Meeting> {
    return this.meetingService.getMeetingById(id, user);
  }

  @ApiOperation({
    summary: '모임 전체 조회/검색/필터링',
    description: '모임 전체 조회/검색/필터링',
  })
  @Get('/')
  getAllMeeting(
    // @Query() pageOptionsDto: PageOptionsDto,
    @Query() getMeetingDto: GetMeetingDto,
  ) {
    return this.meetingService.getAllMeeting(getMeetingDto);
  }

  @ApiOperation({
    summary: '모임 생성',
    description: '모임 생성',
  })
  @ApiConsumes('multipart/form-data')
  @ApiCreatedResponse({
    description: '모임 생성',
    schema: {
      example: {},
    },
  })
  @Post('/')
  @ApiBearerAuth('access-token')
  @UseGuards(AuthGuard('jwt'))
  @HttpCode(200)
  @UseInterceptors(FilesInterceptor('files', 6))
  createMeeting(
    @UploadedFiles() files: Array<Express.MulterS3.File>,
    @Body() createMeetingDto: CreateMeetingDto,
    @GetUser() user: User,
  ): Promise<void> {
    return this.meetingService.createMeeting(createMeetingDto, files, user);
  }

  @ApiOperation({
    summary: '모임 수정',
    description: '모임 수정',
  })
  @ApiConsumes('multipart/form-data')
  @ApiCreatedResponse({
    description: '모임 수정',
    schema: {
      example: {},
    },
  })
  @ApiBearerAuth('access-token')
  @UseGuards(AuthGuard('jwt'))
  @Put('/:id')
  @UseInterceptors(FilesInterceptor('files', 6))
  updateMeetingById(
    @Param('id', ParseIntPipe) id: number,
    @UploadedFiles() files: Array<Express.MulterS3.File>,
    @Body() updateMeetingDto: UpdateMeetingDto,
    @GetUser() user: User,
  ) {
    return this.meetingService.updateMeetingById(
      id,
      updateMeetingDto,
      files,
      user,
    );
  }

  @ApiOperation({
    summary: '모임 삭제',
    description: '모임 삭제',
  })
  @ApiBearerAuth('access-token')
  @UseGuards(AuthGuard('jwt'))
  @ApiParam({ name: 'id', required: true, description: '모임 id' })
  @Delete('/:id')
  deleteMeetingById(@Param('id', ParseIntPipe) id: number): Promise<void> {
    return this.meetingService.deleteMeetingById(id);
  }
}
