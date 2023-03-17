import { IsNotEmpty, IsNumber, IsString } from 'class-validator';

export class PlaygroundRepositoryGetUserProfileCareerDto {
  /** 경력 id */
  @IsNotEmpty()
  @IsNumber()
  id: number;

  /** 회사 이름 */
  @IsNotEmpty()
  @IsString()
  companyName: string;

  /** 경력 이름 */
  @IsNotEmpty()
  @IsString()
  title: string;

  /** 경력 시작일 */
  @IsNotEmpty()
  @IsString()
  startDate: string;

  /** 경력 종료일 */
  @IsNotEmpty()
  @IsString()
  endDate: string;

  /**
   * 경력 진행중 여부
   * TODO: 확인필요
   * */
  @IsNotEmpty()
  @IsString()
  isCurrent: string;
}
