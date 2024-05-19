import { Type } from 'class-transformer';
import {
  IsBoolean,
  IsDate,
  IsDefined,
  IsInstance,
  IsNotEmpty,
  IsString,
  ValidateNested,
} from 'class-validator';

  import { UserPart } from 'src/entity/user/enum/user-part.enum';
  import { PlaygroundRepositoryGetUserActivityInfoDto } from './playground-repository-get-user-activity-info.dto';
  export class  PlaygroundRepositoryGetUserActivityDto {

  /** 유저 아이디 */
  @IsNotEmpty()
  @IsString()
  id: string;

  /** 회원 프로필 */
  @IsNotEmpty()
  @IsString()
  profileImage: string;

  /** 유저 이름 */
  @IsNotEmpty()
  @IsDate()
  name: string;


  @IsInstance(PlaygroundRepositoryGetUserActivityInfoDto, { each: true })
  @ValidateNested({ each: true })
  @Type(() => PlaygroundRepositoryGetUserActivityInfoDto)
  activities: PlaygroundRepositoryGetUserActivityInfoDto[];
  }
  