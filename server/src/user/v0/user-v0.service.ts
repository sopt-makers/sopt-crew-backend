import { HttpException, HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { User } from 'src/entity/user/user.entity';
import { UserRepository } from '../../entity/user/user.repository';
import { getMeetingStatus } from 'src/common/utils/get-meeting-status.function';
import { ApplyRepository } from 'src/entity/apply/apply.repository';
import { MeetingRepository } from 'src/entity/meeting/meeting.repository';

@Injectable()
export class UserV0Service {
  constructor(
    @InjectRepository(UserRepository)
    private userRepository: UserRepository,

    @InjectRepository(ApplyRepository)
    private applyRepository: ApplyRepository,

    @InjectRepository(MeetingRepository)
    private meetingRepository: MeetingRepository,
  ) {}

  // 내가 생성한 모임 조회
  async getMeetingByUser(user: User) {
    const [meetings, itemCount] =
      await this.meetingRepository.getMeetingAndCount(user);

    const resultPromises = meetings.map(async (meeting) => {
      const { status } = await getMeetingStatus(meeting);

      return {
        ...meeting,
        status,
      };
    });

    const result = await Promise.all(resultPromises);
    return { meetings: result, count: itemCount };
  }

  // 내가 지원한 내역 조회
  async getApplyByUser(user: User) {
    const [applies, itemCount] = await this.applyRepository.getApplyAndCount(
      user,
    );

    const resultPromises = applies.map(async (apply) => {
      const { status } = await getMeetingStatus(apply.meeting);

      return {
        ...apply,
        meeting: {
          ...apply.meeting,
          status: status,
        },
      };
    });

    const result = await Promise.all(resultPromises);

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
