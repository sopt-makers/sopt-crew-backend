import {
  Body,
  Controller,
  Delete,
  Get,
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
import { UpdateMeetingDto } from './dto/update-meeting-dto';
import { GetUser } from 'src/auth/get-user.decorator';
import { User } from 'src/users/user.entity';
import { AuthGuard } from '@nestjs/passport';
import {
  ApiTags,
  ApiOperation,
  ApiCreatedResponse,
  ApiConsumes,
  ApiBearerAuth,
  ApiParam,
} from '@nestjs/swagger';
import { ApplyMeetingDto } from './dto/apply-meeting.dto';
import { GetMeetingDto } from './dto/get-meeting.dto';
import { GetListDto } from './dto/get-list.dto';
import { UpdateStatusApplyDto } from './dto/update-status-apply.dto';
import { InviteMeetingDto } from './dto/invite-meeting.dto';
import { UpdateStatusInviteDto } from './dto/update-status-invite.dto';
import { GetUsersDto } from './dto/get-users.dto';

@ApiTags('모임')
@Controller('meeting')
export class MeetingController {
  constructor(private meetingService: MeetingService) {}

  @ApiOperation({
    summary: '모임 초대 사용자 리스트 조회',
    description: '모임 초대 사용자 리스트 조회',
  })
  @ApiParam({ name: 'id', required: true, description: '모임 id' })
  @Get('/:id/users')
  async getInviteUsersByMeeting(
    @Param('id', ParseIntPipe) id: number,
    @Query() getUsersDto: GetUsersDto,
  ) {
    return this.meetingService.getInvitableUsersByMeeting(id, getUsersDto);
  }

  @ApiOperation({
    summary: '모임 지원자 초대 상태 변경',
    description: '모임 지원자 초대 상태 변경',
  })
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Put('/:id/invite/status')
  @ApiParam({ name: 'id', required: true, description: '모임 id' })
  async updateInviteStatusByMeeting(
    @Param('id', ParseIntPipe) id: number,
    @Body() updateStatusInviteDto: UpdateStatusInviteDto,
    @GetUser() user: User,
  ) {
    return this.meetingService.updateInviteStatusByMeeting(
      id,
      user,
      updateStatusInviteDto,
    );
  }

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
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Get('/:id/list')
  @ApiParam({ name: 'id', required: true, description: '모임 id' })
  async getListByMeeting(
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
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Post('/apply')
  async applyMeeting(
    @Body() applyMeetingDto: ApplyMeetingDto,
    @GetUser() user: User,
  ) {
    return this.meetingService.applyMeeting(applyMeetingDto, user);
  }

  @ApiOperation({
    summary: '모임 상세 조회',
    description: '모임 상세 조회',
  })
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @ApiParam({ name: 'id', required: true, description: '모임 id' })
  @Get('/:id')
  async getMeetingById(
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
  async getAllMeeting(
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
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  // @HttpCode(200)
  @UseInterceptors(FilesInterceptor('files', 6))
  async createMeeting(
    @UploadedFiles() files: Array<Express.MulterS3.File>,
    @Body() createMeetingDto: CreateMeetingDto,
    @GetUser() user: User,
  ) {
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
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Put('/:id')
  @UseInterceptors(FilesInterceptor('files', 6))
  async updateMeetingById(
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
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @ApiParam({ name: 'id', required: true, description: '모임 id' })
  @Delete('/:id')
  async deleteMeetingById(
    @Param('id', ParseIntPipe) id: number,
  ): Promise<void> {
    return this.meetingService.deleteMeetingById(id);
  }

  @ApiOperation({
    summary: '모임 초대',
    description: '모임 초대',
  })
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  // @ApiParam({ name: 'id', required: true, description: '모임 id' })
  @Post('/invite')
  async inviteMeeting(
    @Body() inviteMeetingDto: InviteMeetingDto,
  ): Promise<void> {
    return this.meetingService.inviteMeeting(inviteMeetingDto);
  }

  @ApiOperation({
    summary: '모임 초대 취소',
    description: '모임 초대 취소',
  })
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @ApiParam({ name: 'inviteId', required: true, description: '초대 id' })
  @Delete('/:id/invite/:inviteId')
  async cancelInvite(
    @Param('id', ParseIntPipe) id: number,
    @Param('inviteId', ParseIntPipe) inviteId: number,
    @GetUser() user: User,
  ) {
    return this.meetingService.cancelInvite(id, inviteId, user);
  }
}
