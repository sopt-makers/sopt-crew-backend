import { Injectable } from '@nestjs/common';
import { PlaygroundRepository } from './playground.repository';

@Injectable()
export class PlaygroundService {
  constructor(private readonly playgroundRepository: PlaygroundRepository) {}

  async getUser(authToken: string) {
    const response = await this.playgroundRepository.getUser(authToken);

    return response;
  }

  async getUserProfile(authToken: string) {
    const response = await this.playgroundRepository.getUserProfile(authToken);

    return response;
  }

  async getUserActivities(authToken: string, memberId: number) {
    const response = await this.playgroundRepository.getUserActivities(authToken, memberId);

    return response;
  }
}
