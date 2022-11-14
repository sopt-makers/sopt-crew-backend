import { IsNotEmpty, IsNumber, IsString } from 'class-validator';

export class AuthCredentialsDTO {
  @IsString()
  @IsNotEmpty()
  readonly name: string;

  @IsNumber()
  @IsNotEmpty()
  readonly originId: number;
}
