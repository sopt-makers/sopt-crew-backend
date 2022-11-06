import { IsNotEmpty, IsString } from 'class-validator';

export class AuthTokenDTO {
  @IsString()
  readonly accessToken: string;
}
