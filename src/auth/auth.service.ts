import { Injectable, UnauthorizedException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { JwtService } from '@nestjs/jwt';
import { User } from '../users/user.entity';
import { UserRepository } from 'src/users/user.repository';
import { AuthCredentialsDTO } from './dto/auth-credential.dto';

@Injectable()
export class AuthService {
  constructor(
    @InjectRepository(UserRepository)
    private userRepository: UserRepository,
    private jwtService: JwtService,
  ) {}

  async loginUser(authCredentialsDTO: AuthCredentialsDTO) {
    // accessToken으로 유저 정보 받아서
    const { originId, name } = authCredentialsDTO;
    let userId;
    const user = await this.userRepository.findOne({
      where: { originId },
    });

    if (!user) {
      const newUser = this.userRepository.create({
        originId,
        name,
      });
      const savedUser = await this.userRepository.save(newUser);
      userId = savedUser.id;
    } else {
      userId = user.id;
    }

    const payload = { name, id: userId };
    const accessToken = await this.jwtService.sign(payload);
    return { accessToken: accessToken };
  }
}
