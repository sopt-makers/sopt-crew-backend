import { CustomRepository } from 'src/db/typeorm-ex.decorator';
import { User } from 'src/entity/user/user.entity';
import { Repository } from 'typeorm';
import { UserCreateUserDto } from './dto/user-create-user.dto';

@CustomRepository(User)
export class UserRepository extends Repository<User> {
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

  async createUser(createUserDTO: UserCreateUserDto) {
    return this.create(createUserDTO);
  }
}
