import { HttpException, HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { User } from 'src/users/user.entity';
import { UserRepository } from './user.repository';
import { meetingStatus } from 'src/common/utils/meeting.status';

@Injectable()
export class UsersService {
  constructor(
    @InjectRepository(UserRepository)
    private userRepository: UserRepository,
  ) {}

  async getMeetingByUser(user: User) {
    const result = await this.userRepository.getMeetingByUser(user);

    result[0].forEach(async (meeting) => {
      const { status } = await meetingStatus(meeting);
      meeting.status = status;
    });
    return { meetings: result[0], count: result[1] };
  }

  async getApplyByUser(user: User) {
    const result = await this.userRepository.getApplyByUser(user);
    result[0].forEach(async (item) => {
      const { status } = await meetingStatus(item.meeting);
      item.meeting.status = status;
    });

    return { apply: result[0], count: result[1] };
  }

  async getUserById(id: number): Promise<User> {
    const users = this.userRepository.getUserById(id);

    if (!users) {
      throw new HttpException(
        { message: '해당 사용자가 없습니다' },
        HttpStatus.BAD_REQUEST,
      );
    }

    return users;
  }
}
