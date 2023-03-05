import { HttpException, HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { User } from 'src/users/user.entity';
import { GetUsersDto } from './dto/get-users.dto';
import { UserRepository } from './user.repository';
import axios from 'axios';
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

  async getUsers(getUsersDto: GetUsersDto) {
    const { name, generation } = getUsersDto;
    const result = await axios.get<Array<any>>(
      encodeURI(
        name
          ? `https://playground.api.sopt.org/api/v1/members/search?name=${name}`
          : `https://playground.api.sopt.org/api/v1/members/search?name=`,
      ),
    );

    const fin = result.data.filter((item) =>
      generation ? item.generation === generation : item,
    );
    return fin;
  }
}
