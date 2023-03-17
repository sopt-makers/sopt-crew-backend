import { IsNotEmpty, IsNumber, IsString } from 'class-validator';

export class PlaygroundRepositoryGetUserProfileProjectDto {
  /** 프로젝트 id */
  @IsNotEmpty()
  @IsNumber()
  id: number;

  /** 프로젝트 이름 */
  @IsNotEmpty()
  @IsString()
  name: string;

  /** 프로젝트 설명 */
  @IsNotEmpty()
  @IsString()
  summery: string;

  /** 프로젝트 기수 */
  @IsNotEmpty()
  @IsNumber()
  generation: number;

  /** 프로젝트 카테고리 */
  @IsNotEmpty()
  @IsString()
  category: string;

  /** 프로젝트 logoImage */
  @IsNotEmpty()
  @IsString()
  logoImage: string;

  /** 프로젝트 썸네일 */
  @IsNotEmpty()
  @IsString()
  thumbnailImage: string;

  /** 프로젝트 썸네일 */
  @IsNotEmpty({ each: true })
  @IsString({ each: true })
  serviceType: string[];
}
