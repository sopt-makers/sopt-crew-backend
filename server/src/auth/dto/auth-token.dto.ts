import { ApiProperty } from '@nestjs/swagger';
import { IsNotEmpty, IsString } from 'class-validator';

export class AuthTokenDTO {
  @ApiProperty({
    example:
      'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxOCIsImV4cCI6MTY3OTYwOTk3OH0.9D_Tc14J3S0VDmQgT5lUJ5i3KJZob3NKVmSS3fPjHAo',
    description:
      '영우의 playgroud authToken (남용하지 마시오. dev환경에서만 동작)',
    required: true,
  })
  @IsString()
  @IsNotEmpty()
  readonly authToken: string;
}
