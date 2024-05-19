import { Type } from 'class-transformer';
import {
  IsInstance,
  IsNotEmpty,
  IsString,
  ValidateNested,
} from 'class-validator';

export class PlaygroundRepositoryGetUserActivityInfoDto {
  /** 기수 설명 */
  @IsNotEmpty()
  @IsString()
  cardinalInfo: string;

}
