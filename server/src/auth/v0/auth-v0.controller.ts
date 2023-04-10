import { Body, Controller, Post } from '@nestjs/common';
import { AuthV0Service } from './auth-v0.service';
import { AuthV0TokenDto } from './dto/auth-v0-token.dto';

import { ApiTags, ApiOperation } from '@nestjs/swagger';

@ApiTags('인증')
@Controller('auth')
export class AuthV0Controller {
  constructor(private authV0Service: AuthV0Service) {}

  @ApiOperation({
    summary: '로그인/회원가입',
    description: '로그인/회원가입',
  })
  @Post('/')
  async loginUser(@Body() authTokenDTO: AuthV0TokenDto) {
    return this.authV0Service.loginUser(authTokenDTO);
  }
}
