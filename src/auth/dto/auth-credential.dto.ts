import { IsNotEmpty, IsNumber, IsString } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';
import { Type } from 'class-transformer';

export class AuthCredentialsDTO {
  @ApiProperty({
    example: '메이커수',
    description: '사용자 이름',
    required: true,
  })
  @IsString()
  @IsNotEmpty()
  readonly name: string;

  @ApiProperty({
    example: 3,
    description: 'Sopt.org 회원 고유 아이디',
    required: true,
  })
  @Type(() => Number)
  @IsNumber()
  @IsNotEmpty()
  readonly orgId: number;
}
