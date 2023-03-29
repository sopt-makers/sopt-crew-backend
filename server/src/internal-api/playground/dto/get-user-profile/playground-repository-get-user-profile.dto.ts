import { Type } from 'class-transformer';
import {
  IsBoolean,
  IsDate,
  IsInstance,
  IsNotEmpty,
  IsString,
  ValidateNested,
} from 'class-validator';
import { PlaygroundRepositoryGetUserProfileActivityDto } from './playground-repository-get-user-profile-activity.dto';
import { PlaygroundRepositoryGetUserProfileCareerDto } from './playground-repository-get-user-profile-career.dto';
import { PlaygroundRepositoryGetUserProfileLinkDto } from './playground-repository-get-user-profile-link.dto';
import { PlaygroundRepositoryGetUserProfileProjectDto } from './playground-repository-get-user-profile-project.dto';

export class PlaygroundRepositoryGetUserProfileDto {
  /** 유저 이름 */
  @IsNotEmpty()
  @IsString()
  name: string;

  /** 회원 프로필 */
  @IsNotEmpty()
  @IsString()
  profileImage: string;

  /** 생일 */
  @IsNotEmpty()
  @IsDate()
  birthday: Date;

  /** 핸드폰 번호 */
  @IsNotEmpty()
  @IsString()
  phone: string;

  /** 이메일 */
  @IsNotEmpty()
  @IsString()
  email: string;

  /**
   * 주소
   * @description 도 + 시
   * */
  @IsNotEmpty()
  @IsString()
  address: string;

  /** 대학명 */
  @IsNotEmpty()
  @IsString()
  university: string;

  /** 전공명 */
  @IsNotEmpty()
  @IsString()
  major: string;

  /** 자기소개 */
  @IsNotEmpty()
  @IsString()
  introduction: string;

  /** 스킬 */
  @IsNotEmpty()
  @IsString()
  skill: string;

  /** 채용 허용 여부 */
  @IsNotEmpty()
  @IsBoolean()
  openToWork: boolean;

  /** 사이드 프로젝트 허용 여부 */
  @IsNotEmpty()
  @IsBoolean()
  openToSideProject: boolean;

  /** 오피셜 사이트 노출 가능 여부 */
  @IsNotEmpty()
  @IsBoolean()
  allowOfficial: boolean;

  /**
   * 본인 맞는 지 여부..?
   * TODO: 확인 필요
   * */
  @IsNotEmpty()
  @IsBoolean()
  isMine: boolean;

  /** 활동 목록 */
  @IsInstance(PlaygroundRepositoryGetUserProfileActivityDto, { each: true })
  @ValidateNested({ each: true })
  @Type(() => PlaygroundRepositoryGetUserProfileActivityDto)
  activities: PlaygroundRepositoryGetUserProfileActivityDto[];

  /** 링크 목록 */
  @IsInstance(PlaygroundRepositoryGetUserProfileLinkDto, { each: true })
  @ValidateNested({ each: true })
  @Type(() => PlaygroundRepositoryGetUserProfileLinkDto)
  links: PlaygroundRepositoryGetUserProfileLinkDto[];

  /** 프로젝트 목록 */
  @IsInstance(PlaygroundRepositoryGetUserProfileProjectDto, { each: true })
  @ValidateNested({ each: true })
  @Type(() => PlaygroundRepositoryGetUserProfileProjectDto)
  projects: PlaygroundRepositoryGetUserProfileProjectDto[];

  /** 프로젝트 목록 */
  @IsInstance(PlaygroundRepositoryGetUserProfileCareerDto, { each: true })
  @ValidateNested({ each: true })
  @Type(() => PlaygroundRepositoryGetUserProfileCareerDto)
  careers: PlaygroundRepositoryGetUserProfileCareerDto[];
}
