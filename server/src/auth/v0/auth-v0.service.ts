import {
  HttpException,
  HttpStatus,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { JwtService } from '@nestjs/jwt';
import { PlaygroundService } from 'src/internal-api/playground/playground.service';
import { UserActivity } from 'src/entity/user/interface/user-activity.interface';
import { AuthV0TokenDto } from './dto/auth-v0-token.dto';
import { UserRepository } from 'src/entity/user/user.repository';
import { UserPart } from 'src/entity/user/enum/user-part.enum';

@Injectable()
export class AuthV0Service {
  constructor(
    @InjectRepository(UserRepository)
    private userRepository: UserRepository,

    private playgroundService: PlaygroundService,
    private jwtService: JwtService,
  ) {}

  async loginUser(authTokenDTO: AuthV0TokenDto) {
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

    
      const playgroundUserActivities = await this.playgroundService.getUserActivities(
        authToken, user.orgId
      );

      const activities: UserActivity[] = playgroundUserActivities[0].activities.flatMap((activity) => {
        const [generationString, partString] = activity.cardinalInfo.split(',');
        const generation = parseInt(generationString);
        const partKey = getKeyByValue(UserPart, partString);
        const part = UserPart[partKey];

        return {
          generation: generation, 
          part: part 
        };
    });
      
      const phone = playgroundUserProfile.phone
        ? playgroundUserProfile.phone
        : null;

      await this.userRepository.update(
        { id: userId },
        { activities, profileImage, name, phone },
      );

      const payload = { name, id: userId };
      const accessToken = this.jwtService.sign(payload);

      return { accessToken };
    } catch (error) {
      console.log(error);
      if (error.response?.status === HttpStatus.UNAUTHORIZED) {
        throw new UnauthorizedException({ message: '유효하지 않은 토큰' });
      }

      throw new HttpException(
        { message: '로그인 서버 에러' },
        HttpStatus.INTERNAL_SERVER_ERROR,
      );
    }
  }
}

function getKeyByValue(object: any, value: string) {
  return Object.keys(object).find(key => object[key] === value);
}