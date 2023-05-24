import { InjectRepository } from '@nestjs/typeorm';
import dayjs from 'dayjs';
import { NoticeRepository } from 'src/entity/notice/notice.repository';
import { EnGetNoticesStatus } from './enum/get-notices-status.enum';
import { NoticeV1GetNoticesQueryDto } from './dto/notice-v1-get-notices-query.dto';
import { NoticeV1CreateNoticeBodyDto } from './dto/notice-v1-create-notice-body.dto';
import { NoticeV1GetNoticesResponseDto } from './dto/notice-v1-get-notices-response.dto';
import { Injectable } from '@nestjs/common';

@Injectable()
export class NoticeV1Service {
  constructor(
    @InjectRepository(NoticeRepository)
    private readonly noticeRepository: NoticeRepository,
  ) {}

  /** 공지 조회 */
  async getNotices({
    status,
  }: NoticeV1GetNoticesQueryDto): Promise<
    NoticeV1GetNoticesResponseDto[] | null
  > {
    switch (status) {
      case EnGetNoticesStatus.EXPOSING:
        const notices = await this.noticeRepository.getExposingNotices();

        return notices.map((notice) => {
          return {
            id: notice.id,
            title: notice.title,
            subTitle: notice.subTitle,
            contents: notice.contents,
            createdDate: notice.createdDate,
          };
        });
      default:
        return null;
    }
  }

  /** 공지 생성 */
  async createNotice({
    title,
    subTitle,
    contents,
    exposeStartDate,
    exposeEndDate,
  }: NoticeV1CreateNoticeBodyDto): Promise<number> {
    const createdDate = dayjs().toDate();
    const notice = await this.noticeRepository.save({
      title,
      subTitle,
      contents,
      createdDate,
      exposeStartDate,
      exposeEndDate,
    });

    return notice.id;
  }
}
