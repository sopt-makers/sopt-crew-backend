import { Body, Controller, Post } from '@nestjs/common';
import { AuthService } from './auth.service';
import { AuthTokenDTO } from './dto/auth-token.dto';

import { ApiTags, ApiOperation } from '@nestjs/swagger';

@ApiTags('인증')
@Controller('auth')
export class AuthController {
  constructor(private authService: AuthService) {}

  @ApiOperation({
    summary: '로그인/회원가입',
    description: '로그인/회원가입',
  })
  @Post('/')
  async loginUser(@Body() authTokenDTO: AuthTokenDTO) {
    return this.authService.loginUser(authTokenDTO);
  }
}
