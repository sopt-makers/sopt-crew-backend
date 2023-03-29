import { HttpException, HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { User } from 'src/users/user.entity';
import { UserRepository } from './users.repository';
import { meetingStatus } from 'src/common/utils/meeting.status';

@Injectable()
export class UsersService {
  constructor(
    @InjectRepository(UserRepository)
    private userRepository: UserRepository,
  ) {}

  // 내가 생성한 모임 조회
  async getMeetingByUser(user: User) {
    const [meetings, itemCount] = await this.userRepository.getMeetingAndCount(
      user,
    );

    const result = meetings.map(async (meeting) => {
      const { status } = await meetingStatus(meeting);

      return {
        ...meeting,
        status,
      };
    });
    return { meetings: result, count: itemCount };
  }

  // 내가 지원한 내역 조회
  async getApplyByUser(user: User) {
    const [applies, itemCount] = await this.userRepository.getApplyAndCount(
      user,
    );

    const result = applies.map(async (apply) => {
      const { status } = await meetingStatus(apply.meeting);

      return {
        ...apply,
        meeting: {
          ...apply.meeting,
          status: status,
        },
      };
    });

    return { apply: result, count: itemCount };
  }

  // 유저 정보 조회
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
