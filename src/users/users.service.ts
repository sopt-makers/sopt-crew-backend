import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { User } from 'src/users/user.entity';
import { GetUsersDto } from './dto/get-users.dto';
import { UserRepository } from './user.repository';
import axios from 'axios';

@Injectable()
export class UsersService {
  constructor(
    @InjectRepository(UserRepository)
    private userRepository: UserRepository,
  ) {}

  async getMeetingByUser(user: User) {
    return this.userRepository.getMeetingByUser(user);
  }

  async getApplyByUser(user: User) {
    return this.userRepository.getApplyByUser(user);
  }

  async getUserById(id: number): Promise<User> {
    return this.userRepository.getUserById(id);
  }

  async getUsers(getUsersDto: GetUsersDto) {
    const { name, generation } = getUsersDto;
    const result = await axios.get<Array<any>>(
      encodeURI(
        `https://playground.api.sopt.org/api/v1/members/search?name=${name}`,
      ),
    );

    const fin = result.data.filter((item) =>
      generation ? item.generation === generation : item,
    );
    return fin;
  }
}
