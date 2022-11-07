import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { User } from 'src/users/user.entity';
import { UserRepository } from './user.repository';

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
}
