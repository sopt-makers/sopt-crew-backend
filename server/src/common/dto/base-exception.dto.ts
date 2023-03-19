import { ApiProperty } from '@nestjs/swagger';
import { IsNotEmpty, IsString } from 'class-validator';

export class BaseExceptionDto {
  @ApiProperty()
  @IsNotEmpty()
  @IsString()
  message: string;
}
