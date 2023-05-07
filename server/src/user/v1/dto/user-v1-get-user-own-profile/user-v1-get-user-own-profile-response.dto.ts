import { ApiProperty } from '@nestjs/swagger';
import { Allow, IsNotEmpty, IsNumber, IsString } from 'class-validator';

export class UserV1GetUserOwnProfileResponseDto {
  @ApiProperty({
    description: 'Crew ID',
    example: 1,
  })
  @IsNotEmpty()
  @IsNumber()
  id: number;

  @ApiProperty({
    description: 'Playground ID',
    example: 1,
  })
  @IsNotEmpty()
  @IsNumber()
  orgId: number;

  @ApiProperty({
    description: '유저 이름',
    example: '강영우',
  })
  @IsNotEmpty()
  @IsString()
  name: string;

  @ApiProperty({
    description: '유저 이미지',
  })
  @IsNotEmpty()
  @Allow(null)
  @IsString()
  profileImage: string | null;
}
