import { ApiProperty } from '@nestjs/swagger';
import { IsNotEmpty, IsString } from 'class-validator';

export class AuthTokenDTO {
  @ApiProperty({
    example:
      'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoibGUzZTMiLCJ1c2VySWQiOjIsImlhdCI6MTY2ODQzMzg0OSwiZXhwIjoxNzA0NDMzODQ5fQ.Xd69DWu7Z5FYECeSWcReGUoflq2UOiEOpVa_vdjRJEY',
    description: 'playgroud authToken',
    required: true,
  })
  @IsString()
  @IsNotEmpty()
  readonly authToken: string;
}
