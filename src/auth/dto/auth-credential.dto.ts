import { IsNotEmpty, IsString } from 'class-validator';

export class AuthCredentialsDTO {
  @IsString()
  readonly name: string;

  @IsString()
  readonly password: string;
}
