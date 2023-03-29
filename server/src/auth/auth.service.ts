import { HttpException, HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { JwtService } from '@nestjs/jwt';
import { UserRepository } from 'src/users/users.repository';
import { AuthTokenDTO } from './dto/auth-token.dto';
import { PlaygroundService } from 'src/internal-api/playground/playground.service';
import { UserActivity } from 'src/users/interface/user-activity.interface';

@Injectable()
export class AuthService {
  constructor(
    @InjectRepository(UserRepository)
    private userRepository: UserRepository,
    private playgroundService: PlaygroundService,
    private jwtService: JwtService,
  ) {}

  async loginUser(authTokenDTO: AuthTokenDTO) {
    const { authToken } = authTokenDTO;

    try {
      const { id, name, profileImage } = await this.playgroundService.getUser(
        authToken,
      );
      const user = await this.userRepository.getUserByOrgId(id);
      const userId: number = await (async () => {
        if (!user) {
          const newUser = await this.userRepository.save({
            orgId: id,
            name,
            profileImage,
          });

          return newUser.id;
        }

        return user.id;
      })();

      const playgroundUserProfile = await this.playgroundService.getUserProfile(
        authToken,
      );
      const activities: UserActivity[] =
        playgroundUserProfile.activities.flatMap((activity) => {
          return activity.cardinalActivities.map((cardinalActivity) => ({
            generation: cardinalActivity.generation,
            part: cardinalActivity.part,
          }));
        });
      await this.userRepository.update(
        { id: userId },
        { activities, profileImage, name },
      );

      const payload = { name, id: userId };
      const accessToken = this.jwtService.sign(payload);

      return { accessToken };
    } catch (error) {
      console.log(error);

      throw new HttpException(
        { message: '로그인 서버 에러' },
        HttpStatus.INTERNAL_SERVER_ERROR,
      );
    }
  }
}
