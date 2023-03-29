import { IsBoolean, IsNotEmpty, IsNumber, IsString } from 'class-validator';

export class PlaygroundRepositoryGetUserProfileActivityCardinalActivityDto {
  /** 활동 식별자 */
  @IsNotEmpty()
  @IsNumber()
  id: number;

  /** 활동 기수 */
  @IsNotEmpty()
  @IsNumber()
  generation: number;

  /** 활동 팀 */
  @IsNotEmpty()
  @IsString()
  team: string;

  /** 활동 파트 */
  @IsNotEmpty()
  @IsString()
  part: string;

  /** 프로젝트 여부 */
  @IsNotEmpty()
  @IsBoolean()
  isProject: boolean;
}
