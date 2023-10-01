import { CustomRepository } from 'src/db/typeorm-ex.decorator';
import { User } from 'src/entity/user/user.entity';
import { DeleteResult, Repository } from 'typeorm';
import { Apply } from 'src/entity/apply/apply.entity';
import { ApplyType } from './enum/apply-type.enum';
import { ApplyStatus } from './enum/apply-status.enum';
import dayjs from 'dayjs';
import { MeetingV0ApplyMeetingDto } from 'src/meeting/v0/dto/meeting-v0-apply-meeting.dto';
import { Meeting } from '../meeting/meeting.entity';
import { MeetingV0ListDate } from 'src/meeting/v0/enum/meeting-v0-list-date.enum';

@CustomRepository(Apply)
export class ApplyRepository extends Repository<Apply> {
  // 지원 조회
  async getApply(id: number, type = ApplyType.APPLY): Promise<Apply> {
    const apply = await this.findOne({
      where: { id: id, type },
      relations: ['user'],
    });

    return apply;
  }

  async getApplyAndCount(user: User): Promise<[Apply[], number]> {
    const { id } = user;
    const result = await this.createQueryBuilder('apply')
      .leftJoinAndSelect('apply.meeting', 'meeting')
      .leftJoinAndSelect('meeting.user', 'user')
      .leftJoinAndSelect(
        'meeting.appliedInfo',
        'appliedInfo',
        'appliedInfo.status = :status',
        { status: 1 },
      )
      .where('apply.userId = :id', { id })
      .getManyAndCount();

    return result;
  }

  // 조건에 맞는 지원들 and 개수 조회
  async getAppliesAndCount(
    meetingId: number,
    typeArr: ApplyType[],
    statusArr: ApplyStatus[],
    skip: number,
    take?: number,
    date?: MeetingV0ListDate,
  ) {
    const orderByAppliedDate = (() => {
      switch (date) {
        case MeetingV0ListDate.DESC:
          return 'DESC';
        case MeetingV0ListDate.ASC:
        default:
          return 'ASC';
      }
    })();

    const applyQuery = this.createQueryBuilder('apply')
      .select([
        'apply.id',
        'apply.type',
        'apply.appliedDate',
        'apply.content',
        'apply.status',
      ])
      .leftJoinAndSelect('apply.user', 'user')
      .where('apply.meetingId = :id', { id: meetingId })
      .andWhere('apply.type IN(:...type)', { type: typeArr })
      .andWhere('apply.status IN(:...status)', { status: statusArr })
      .orderBy('apply.id', orderByAppliedDate);

    applyQuery.skip(skip).take(take);

    return await applyQuery.getManyAndCount();
  }

  // 지원 생성
  async createApply(
    applyMeetingDto: MeetingV0ApplyMeetingDto,
    meeting: Meeting,
    user: User,
    type = ApplyType.APPLY,
  ): Promise<Apply> {
    const { content } = applyMeetingDto;
    const appliedDate = dayjs().toDate();

    const apply = await Apply.createApply(
      user,
      content,
      meeting,
      type,
      appliedDate,
    );

    return apply;
  }

  // 지원 삭제
  async deleteApply(id: number): Promise<DeleteResult> {
    const result = await this.delete({ id });

    return result;
  }
}
