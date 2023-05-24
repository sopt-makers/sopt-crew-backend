import { Repository } from 'typeorm';
import { Notice } from './notice.entity';
import dayjs from 'dayjs';
import { CustomRepository } from 'src/db/typeorm-ex.decorator';

@CustomRepository(Notice)
export class NoticeRepository extends Repository<Notice> {
  public async getExposingNotices(): Promise<Notice[]> {
    const nowDate = dayjs();

    const notices = await this.createQueryBuilder('notice')
      .where('notice.exposeStartDate <= :nowDate', {
        nowDate: nowDate.toDate(),
      })
      .andWhere('notice.exposeEndDate >= :nowDate', {
        nowDate: nowDate.toDate(),
      })
      .getMany();

    return notices;
  }
}
