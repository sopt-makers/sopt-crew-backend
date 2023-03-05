import { CustomRepository } from 'src/db/typeorm-ex.decorator';
import { User } from 'src/users/user.entity';
import { Repository } from 'typeorm';
import { Apply } from 'src/meeting/apply.entity';
import { Meeting } from 'src/meeting/meeting.entity';
import { GetUsersDto } from './dto/get-users.dto';

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

    return result;
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

    return result;
  }

  async getUserById(id: number): Promise<User> {
    const users = await this.findOne({
      where: { id },
      relations: ['apply'],
    });

    return users;
  }

  async getUsers(getUsersDto: GetUsersDto): Promise<User[]> {
    return await this.find();
  }
}
