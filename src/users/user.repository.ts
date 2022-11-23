import { CustomRepository } from 'src/db/typeorm-ex.decorator';
import { User } from 'src/users/user.entity';
import { DataSource, Repository } from 'typeorm';
import {
  ConflictException,
  InternalServerErrorException,
} from '@nestjs/common';
import * as bcrypt from 'bcryptjs';
import { Apply } from 'src/meeting/apply.entity';
import { Meeting } from 'src/meeting/meeting.entity';
import { meetingStatus } from 'src/common/utils/meeting.status';

@CustomRepository(User)
export class UserRepository extends Repository<User> {
  async getMeetingByUser(user: User) {
    const { id } = user;
    const result = await Meeting.createQueryBuilder('meeting')
      .leftJoinAndSelect(
        'meeting.appliedInfo',
        'appliedInfo',
        'appliedInfo.status = :status',
        { status: 1 },
      )
      .leftJoinAndSelect('meeting.user', 'user')
      .where('meeting.userId = :id', { id })
      .getManyAndCount();

    result[0].forEach(async (meeting) => {
      const { status } = await meetingStatus(meeting);
      meeting.status = status;
    });
    return { meetings: result[0], count: result[1] };
  }

  async getApplyByUser(user: User) {
    const { id } = user;
    const result = await Apply.createQueryBuilder('apply')
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

    result[0].forEach(async (item) => {
      const { status } = await meetingStatus(item.meeting);
      item.meeting.status = status;
    });

    return { apply: result[0], count: result[1] };
  }

  async getUserById(id: number): Promise<User> {
    return await this.findOne({
      where: { id },
      relations: ['apply'],
    });
  }
}
