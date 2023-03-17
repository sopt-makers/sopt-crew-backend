import { Type } from 'class-transformer';
import {
  IsInstance,
  IsNotEmpty,
  IsString,
  ValidateNested,
} from 'class-validator';
import { PlaygroundRepositoryGetUserProfileActivityCardinalActivityDto } from './playground-repository-get-user-profile-activity-cardinal-activity.dto';

export class PlaygroundRepositoryGetUserProfileActivityDto {
  /** 기수 설명 */
  @IsNotEmpty()
  @IsString()
  cardinalInfo: string;

  /** 기수 정보 */
  @IsInstance(PlaygroundRepositoryGetUserProfileActivityCardinalActivityDto, {
    each: true,
  })
  @ValidateNested({ each: true })
  @Type(() => PlaygroundRepositoryGetUserProfileActivityCardinalActivityDto)
  cardinalActivities: PlaygroundRepositoryGetUserProfileActivityCardinalActivityDto[];
}
