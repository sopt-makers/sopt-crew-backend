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
import { FilterMeetingDto } from './dto/filter-meeting.dto';
import { GetUser } from 'src/auth/get-user.decorator';
import { User } from 'src/auth/user.entity';
import { AuthGuard } from '@nestjs/passport';
import {
  ApiTags,
  ApiOperation,
  ApiCreatedResponse,
  ApiSecurity,
  ApiExcludeEndpoint,
} from '@nestjs/swagger';

@Controller('meeting')
export class MeetingController {
  constructor(private meetingService: MeetingService) {}

  @UseGuards(AuthGuard('jwt'))
  @Post('/apply')
  applyMeeting(
    @Body('id') id: number,
    @Body('content') content: string,
    @GetUser() user: User,
  ) {
    return this.meetingService.applyMeeting(id, content, user);
  }

  @Post('/filter')
  searchMeetingByFilter(
    @Body() filterMeetingDto: FilterMeetingDto,
  ): Promise<Meeting[]> {
    return this.meetingService.searchMeetingByFilter(filterMeetingDto);
  }

  @Post('/search')
  searchMeeting(
    @Query('query') query: string,
    @Body() filterMeetingDto: FilterMeetingDto,
  ): Promise<Meeting[]> {
    return this.meetingService.searchMeeting(query, filterMeetingDto);
  }

  @Get('/:id')
  getMeetingById(@Param('id', ParseIntPipe) id: number): Promise<Meeting> {
    return this.meetingService.getMeetingById(id);
  }

  @Get('/')
  getAllMeeting(): Promise<Meeting[]> {
    return this.meetingService.getAllMeeting();
  }

  // @UseGuards(AuthGuard('jwt'))
  @ApiOperation({
    summary: '모임 생성',
    description: '모임 생성',
  })
  // @ApiSecurity('X-API-KEY', ['X-API-KEY'])
  @ApiCreatedResponse({
    description: '모임 생성',
    schema: {
      example: {},
    },
  })
  @Post('/')
  @HttpCode(200)
  @UseInterceptors(FilesInterceptor('files', 6))
  createMeeting(
    @UploadedFiles() files: Array<Express.MulterS3.File>,
    @Body() createMeetingDto: CreateMeetingDto,
    @GetUser() user: User,
  ): Promise<Meeting> {
    return this.meetingService.createMeeting(createMeetingDto, files, user);
  }

  @Put('/:id')
  @UseInterceptors(FilesInterceptor('files', 6))
  updateMeetingById(
    @Param('id', ParseIntPipe) id: number,
    @UploadedFiles() files: Array<Express.MulterS3.File>,
    @Body() updateMeetingDto: UpdateMeetingDto,
  ): Promise<void> {
    return this.meetingService.updateMeetingById(id, updateMeetingDto, files);
  }

  @Delete('/:id')
  deleteMeetingById(@Param('id', ParseIntPipe) id: number): Promise<void> {
    return this.meetingService.deleteMeetingById(id);
  }
}
