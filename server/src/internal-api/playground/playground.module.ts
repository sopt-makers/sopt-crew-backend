import { Module } from '@nestjs/common';
import { PlaygroundRepository } from './playground.repository';
import { PlaygroundService } from './playground.service';

@Module({
  imports: [],
  providers: [PlaygroundService, PlaygroundRepository],
  exports: [PlaygroundService],
})
export class PlaygroundModule {}
