import { IsNotEmpty, IsNumber, IsString } from 'class-validator';

export class PlaygroundRepositoryGetUserProfileLinkDto {
  /** 링크 id */
  @IsNotEmpty()
  @IsNumber()
  id: number;

  /** 링크 이름 */
  @IsNotEmpty()
  @IsString()
  title: string;

  /** 링크 id */
  @IsNotEmpty()
  @IsString()
  url: string;
}
