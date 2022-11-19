import { ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsEnum, IsInt, IsOptional, Max, Min } from 'class-validator';

export enum Order {
  ASC = 'ASC',
  DESC = 'DESC',
}

export class PageOptionsDto {
  // @ApiPropertyOptional({
  //   example: '스터디,번개',
  //   description: '정렬 순서',
  //   enum: Order,
  //   default: Order.ASC,
  // })
  // @IsEnum(Order)
  // @IsOptional()
  // readonly order?: Order = Order.ASC;

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
