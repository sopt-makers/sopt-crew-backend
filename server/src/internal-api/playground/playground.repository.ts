import { Injectable, InternalServerErrorException } from '@nestjs/common';
import axios from 'axios';
import { PlaygroundRepositoryGetUserProfileDto } from './dto/get-user-profile/playground-repository-get-user-profile.dto';
import { PlaygroundRepositoryGetUserDto } from './dto/get-user/playground-repository-get-user.dto';

@Injectable()
export class PlaygroundRepository {
  private URL: string =
    process.env.NODE_ENV === 'dev'
      ? 'https://playground.dev.sopt.org'
      : 'https://playground.api.sopt.org';

  async getUser(authToken: string): Promise<PlaygroundRepositoryGetUserDto> {
    try {
      const result = await axios.get(`${this.URL}/internal/api/v1/members/me`, {
        headers: {
          Authorization: authToken,
        },
      });

      return result.data;
    } catch (error) {
      throw new InternalServerErrorException(error);
    }
  }

  async getUserProfile(
    authToken: string,
  ): Promise<PlaygroundRepositoryGetUserProfileDto> {
    try {
      const result = await axios.get(
        `${this.URL}/internal/api/v1/members/profile/me`,
        {
          headers: {
            Authorization: authToken,
          },
        },
      );

      return result.data;
    } catch (error) {
      throw new InternalServerErrorException(error);
    }
  }
}
