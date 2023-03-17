import { CustomRepository } from 'src/db/typeorm-ex.decorator';
import { User } from 'src/users/user.entity';
import { Repository } from 'typeorm';
import { Apply } from 'src/meeting/apply.entity';
import { Meeting } from 'src/meeting/meeting.entity';
import { CreateUserDto } from './dto/create-user.dto';

@CustomRepository(User)
export class UserRepository extends Repository<User> {
  async getMeetingAndCount(user: User): Promise<[Meeting[], number]> {
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

  async getApplyAndCount(user: User): Promise<[Apply[], number]> {
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

  async getUserByOrgId(orgId: number): Promise<User> {
    const users = await this.findOne({
      where: { orgId },
      relations: ['apply'],
    });

    return users;
  }

  async getUsers(ids: Array<number>): Promise<User[]> {
    const users = await User.createQueryBuilder('user')
      .where('user.orgId IN (:...ids)', {
        ids,
      })
      .getMany();
    return users;
  }

  async createUser(createUserDTO: CreateUserDto) {
    return this.create(createUserDTO);
  }
}
