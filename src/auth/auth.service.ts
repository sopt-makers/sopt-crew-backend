import { HttpException, HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { JwtService } from '@nestjs/jwt';
import { UserRepository } from 'src/users/users.repository';
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
      const result = await axios.get<{
        id: number;
        name: string;
        profileImage: string;
      }>('https://playground.api.sopt.org/api/v1/members/me', {
        headers: {
          Authorization: `${authToken}`,
        },
      });

      const { id, name, profileImage } = result.data;
      const user = await this.userRepository.getUser(id);

      if (!user) {
        const newUser = await this.userRepository.createUser({
          orgId: id,
          name,
          profileImage,
        });
        userId = newUser.id;
      } else {
        userId = user.id;
      }

      const payload = { name, id: userId };
      const accessToken = await this.jwtService.sign(payload);
      return { accessToken: accessToken };
    } catch (error) {
      throw new HttpException(
        { message: '로그인 서버 에러' },
        HttpStatus.INTERNAL_SERVER_ERROR,
      );
    }
  }
}
