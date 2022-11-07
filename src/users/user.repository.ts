import { CustomRepository } from 'src/db/typeorm-ex.decorator';
import { User } from 'src/users/user.entity';
import { Repository } from 'typeorm';
import {
  ConflictException,
  InternalServerErrorException,
} from '@nestjs/common';
import * as bcrypt from 'bcryptjs';

@CustomRepository(User)
export class UserRepository extends Repository<User> {
  async getMeetingByUser(user: User) {
    const { id } = user;
    return await this.findOne({
      where: { id },
      relations: ['meetings'],
      select: {
        id: true,
      },
    });
  }

  async getApplyByUser(user: User) {
    const { id } = user;
    return await this.findOne({
      where: { id },
      relations: ['apply'],
      select: ['id'],
    });
  }

  async getUserById(id: number): Promise<User> {
    return await this.findOne({
      where: { id },
      relations: ['apply'],
    });
  }
}
