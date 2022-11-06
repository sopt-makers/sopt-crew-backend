import { CustomRepository } from 'src/db/typeorm-ex.decorator';
import { User } from 'src/auth/user.entity';
import { Repository } from 'typeorm';
import { AuthCredentialsDTO } from './dto/auth-credential.dto';
import {
  ConflictException,
  InternalServerErrorException,
} from '@nestjs/common';
import * as bcrypt from 'bcryptjs';

@CustomRepository(User)
export class UserRepository extends Repository<User> {
  async createUser(authCredentialsDTO: AuthCredentialsDTO): Promise<void> {
    const { name, password } = authCredentialsDTO;
    const salt = await bcrypt.genSalt();
    // const hashedPassword = await bcrypt.hash(password, salt);

    const user = new User();
    user.name = name;

    try {
      const result = await this.save(user);
      console.log(result);
    } catch (error) {
      if (error.code === '23505') {
        throw new ConflictException('이미 존재하는 아이디');
      } else {
        throw new InternalServerErrorException('eee');
      }
    }
  }

  async getUserById(id: number): Promise<User> {
    return await this.findOne({
      where: { id },
      relations: ['apply'],
    });
  }
}
