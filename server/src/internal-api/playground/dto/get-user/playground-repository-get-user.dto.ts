import { IsBoolean, IsNotEmpty, IsNumber, IsString } from 'class-validator';

export class PlaygroundRepositoryGetUserDto {
  /** 유저 id */
  @IsNotEmpty()
  @IsNumber()
  id: number;

  /** 유저 이름 */
  @IsNotEmpty()
  @IsString()
  name: string;

  /** 기수 */
  @IsNotEmpty()
  @IsNumber()
  generation: number;

  /** 유저 이름 */
  @IsNotEmpty()
  @IsString()
  profileImage: string;

  /** 프로필 등록 여부 */
  @IsNotEmpty()
  @IsBoolean()
  hasProfile: boolean;
}
