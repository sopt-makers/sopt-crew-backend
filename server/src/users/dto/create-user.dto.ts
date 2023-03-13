import { IsString, IsNumber } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class CreateUserDto {
  @ApiProperty({
    example: 3,
    description: 'sopt org unique id',
    required: true,
  })
  @IsNumber()
  readonly orgId: number;

  @ApiProperty({
    example: '이동',
    description: '이름 검색',
    required: true,
  })
  @IsString()
  readonly name: string;

  @ApiProperty({
    example: 'http://s3',
    description: '프로필 이미지',
    required: true,
  })
  @IsString()
  readonly profileImage: string;
}
