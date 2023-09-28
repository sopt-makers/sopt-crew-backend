import { HttpStatus } from '@nestjs/common';
import { ApiProperty } from '@nestjs/swagger';

/** 기본 응답 Wrapper */
export class CommonResponseDto<T> {
  @ApiProperty({
    enum: HttpStatus,
    example: HttpStatus.OK,
  })
  statusCode: HttpStatus;

  @ApiProperty()
  data: T;
}
