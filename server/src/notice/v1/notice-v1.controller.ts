import { Body, Controller, Get, Post, Query, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { NoticeV1Service } from './notice-v1.service';
import { AuthGuard } from '@nestjs/passport';
import { NoticeV1GetNoticesQueryDto } from './dto/notice-v1-get-notices-query.dto';
import { NoticeV1CreateNoticeBodyDto } from './dto/notice-v1-create-notice-body.dto';
import { NoticeV1GetNoticesResponseDto } from './dto/notice-v1-get-notices-response.dto';
import { ApiOkResponseCommon } from 'src/common/decorator/api-ok-response-common.decorator';

@ApiTags('공지사항')
@Controller('notice/v1')
export class NoticeV1Controller {
  constructor(private readonly noticeV1Service: NoticeV1Service) {}

  @ApiOperation({ summary: '공지사항 조회' })
  @ApiOkResponseCommon(NoticeV1GetNoticesResponseDto)
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Get()
  async getNotices(
    @Query() query: NoticeV1GetNoticesQueryDto,
  ): Promise<NoticeV1GetNoticesResponseDto[] | null> {
    return this.noticeV1Service.getNotices({ status: query.status });
  }

  @ApiOperation({ summary: '공지사항 작성' })
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Post()
  async createNotice(
    @Body() body: NoticeV1CreateNoticeBodyDto,
  ): Promise<number> {
    return this.noticeV1Service.createNotice(body);
  }
}
