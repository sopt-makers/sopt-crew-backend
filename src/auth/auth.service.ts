import {
  HttpException,
  HttpStatus,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { JwtService } from '@nestjs/jwt';
import { UserRepository } from 'src/users/user.repository';
import { AuthCredentialsDTO } from './dto/auth-credential.dto';
import { AuthTokenDTO } from './dto/auth-token.dto';
import axios from 'axios';

@Injectable()
export class AuthService {
  constructor(
    @InjectRepository(UserRepository)
    private userRepository: UserRepository,
    private jwtService: JwtService,
  ) {}

  async loginUser(authTokenDTO: AuthTokenDTO) {
    const { authToken } = authTokenDTO;
    let userId;
    try {
      const result = await axios.get<{ id: number; name: string }>(
        'https://playground.api.sopt.org/api/v1/members/me',
        {
          headers: {
            Authorization: `${authToken}`,
          },
        },
      );

      const { id, name } = result.data;

      const user = await this.userRepository.findOne({
        where: { orgId: id },
      });
      if (!user) {
        const newUser = await this.userRepository.create({
          orgId: id,
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
    } catch (error) {
      console.log(error);
      throw new HttpException(
        { message: '로그인 서버 에러' },
        HttpStatus.INTERNAL_SERVER_ERROR,
      );
    }
  }
}
