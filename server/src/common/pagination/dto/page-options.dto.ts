import { ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsInt, IsOptional, Max, Min } from 'class-validator';

export class PageOptionsDto {
  @ApiPropertyOptional({
    minimum: 1,
    default: 1,
    example: 1,
    description: '각 페이지',
  })
  @Type(() => Number)
  @IsInt()
  @Min(1)
  @IsOptional()
  readonly page?: number = 1;

  @ApiPropertyOptional({
    minimum: 1,
    maximum: 50,
    default: 12,
    example: 12,
  })
  @Type(() => Number)
  @IsInt()
  @Min(1)
  @Max(50)
  @IsOptional()
  readonly take?: number = 12;

  get skip(): number {
    return (this.page - 1) * this.take;
  }
}
