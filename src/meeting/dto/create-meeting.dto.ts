import { IsNotEmpty, IsDate, IsString, IsOptional } from 'class-validator';
import { Type } from 'class-transformer';

export class CreateMeetingDto {
  @IsString()
  @IsNotEmpty()
  readonly title: string;

  @IsString()
  @IsNotEmpty()
  readonly category: string;

  @Type(() => Date)
  @IsDate()
  @IsNotEmpty()
  readonly startDate: Date;

  @Type(() => Date)
  @IsDate()
  @IsNotEmpty()
  readonly endDate: Date;

  @Type(() => Number) // number 변환
  @IsNotEmpty()
  readonly capacity: number;

  @IsString()
  readonly desc: string;

  @IsString()
  readonly processDesc: string;

  @Type(() => Date)
  @IsDate()
  @IsNotEmpty()
  readonly mStartDate: Date;

  @Type(() => Date)
  @IsDate()
  @IsNotEmpty()
  readonly mEndDate: Date;

  @IsString()
  @IsNotEmpty()
  readonly leaderDesc: string;

  @IsString()
  @IsNotEmpty()
  readonly targetDesc: string;

  @IsString()
  @IsOptional()
  readonly note: string;
}
