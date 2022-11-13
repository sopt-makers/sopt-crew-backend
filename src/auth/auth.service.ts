import { Injectable, UnauthorizedException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { JwtService } from '@nestjs/jwt';
import { User } from '../users/user.entity';
import { UserRepository } from 'src/users/user.repository';

@Injectable()
export class AuthService {
  constructor(
    @InjectRepository(UserRepository)
    private userRepository: UserRepository,
    private jwtService: JwtService,
  ) {}

  async loginUser(accessToken: string) {
    // accessToken으로 유저 정보 받아서

    const originId = '1';
    const id = 3;
    const name = 'lee';

    const user = await this.userRepository.findOne({ where: { id } });

    if (!user) {
      const newUser = this.userRepository.create({
        originId,
        name,
      });
      await this.userRepository.save(newUser);
    }

    const payload = { name, id };
    const accessToken1 = await this.jwtService.sign(payload);
    return { accessToken1 };
    // else {
    //   throw new UnauthorizedException('로그인 실패');
    // }
    return null;
  }
}
