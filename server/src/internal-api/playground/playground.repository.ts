import { Injectable } from '@nestjs/common';
import axios from 'axios';
import { PlaygroundRepositoryGetUserProfileDto } from './dto/get-user-profile/playground-repository-get-user-profile.dto';
import { PlaygroundRepositoryGetUserDto } from './dto/get-user/playground-repository-get-user.dto';
import { PlaygroundRepositoryGetUserActivityDto } from './dto/get-user-activities/playground-repository-get-user-activity.dto';
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
      throw error;
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
      throw error;
    }
  }


  async getUserActivities(
    authToken: string,
    memberId: number,
  ): Promise<PlaygroundRepositoryGetUserActivityDto> {
    try {
      const result = await axios.get(
        `${this.URL}/internal/api/v1/members/profile`,
        {
          headers: {
            Authorization: authToken,
          },
          params: {
            memberIds: memberId,
          },
        },
      );

      return result.data;
    } catch (error) {
      throw error;
    }
  }
}
