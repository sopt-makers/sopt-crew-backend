import { ApiProperty } from '@nestjs/swagger';
import {
  Allow,
  IsBoolean,
  IsNotEmpty,
  IsNumber,
  IsString,
} from 'class-validator';

export class  UserV1GetUserOwnProfileResponseDto {
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

  @ApiProperty({
    description: '유저 활동 가지고 있는지 여부, 유저 활동이 없으면 false',
  })
  @IsNotEmpty()
  @IsBoolean()
  hasActivities: boolean;
}
