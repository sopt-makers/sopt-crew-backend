import { ApiProperty } from '@nestjs/swagger';
import { PageMetaDtoParameters } from '../interface';

export class PageMetaDto {
  @ApiProperty({
    description: '페이지 위치',
  })
  readonly page: number;

  @ApiProperty({
    description: '가져올 데이터 개수',
  })
  readonly take: number;

  @ApiProperty({
    description: '응답 데이터 개수',
  })
  readonly itemCount: number;

  @ApiProperty({
    description: '총 페이지 수',
  })
  readonly pageCount: number;

  @ApiProperty({
    description: '이전 페이지가 있는지 유무',
  })
  readonly hasPreviousPage: boolean;

  @ApiProperty({
    description: '다음페이지가 있는지 유무',
  })
  readonly hasNextPage: boolean;

  constructor({ pageOptionsDto, itemCount }: PageMetaDtoParameters) {
    this.page = pageOptionsDto.page;
    this.take = pageOptionsDto.take;
    this.itemCount = itemCount;
    this.pageCount = Math.ceil(this.itemCount / this.take);
    this.hasPreviousPage = this.page > 1;
    this.hasNextPage = this.page < this.pageCount;
  }
}
