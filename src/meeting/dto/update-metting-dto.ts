import { IsNotEmpty, IsDate, IsString, IsOptional } from 'class-validator';
import { Type } from 'class-transformer';

export class UpdateMeetingDto {
  @IsString()
  @IsNotEmpty()
  @IsOptional()
  readonly title: string;

  @IsString()
  @IsNotEmpty()
  @IsOptional()
  readonly category: string;

  @Type(() => Date)
  @IsDate()
  @IsNotEmpty()
  @IsOptional()
  readonly startDate: Date;

  @Type(() => Date)
  @IsDate()
  @IsNotEmpty()
  @IsOptional()
  readonly endDate: Date;

  @Type(() => Number) // number 변환
  @IsNotEmpty()
  @IsOptional()
  readonly capacity: number;

  @IsString()
  @IsOptional()
  readonly desc: string;

  @IsString()
  @IsOptional()
  readonly processDesc: string;

  @Type(() => Date)
  @IsDate()
  @IsNotEmpty()
  @IsOptional()
  readonly mStartDate: Date;

  @Type(() => Date)
  @IsDate()
  @IsNotEmpty()
  @IsOptional()
  readonly mEndDate: Date;

  @IsString()
  @IsNotEmpty()
  @IsOptional()
  readonly leaderDesc: string;

  @IsString()
  @IsNotEmpty()
  @IsOptional()
  readonly targetDesc: string;

  @IsString()
  @IsOptional()
  readonly note: string;
}
