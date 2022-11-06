import { IsNotEmpty, IsDate, IsString, IsOptional } from 'class-validator';
import { Type } from 'class-transformer';

export class FilterMeetingDto {
  @IsNotEmpty()
  readonly category: string[];

  @IsNotEmpty()
  readonly status: number;
}
