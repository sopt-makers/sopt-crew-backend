import { Body, Controller, HttpStatus, Post } from '@nestjs/common';
import { AuthV0Service } from './auth-v0.service';
import { AuthV0TokenDto } from './dto/auth-v0-token.dto';

import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  getSchemaPath,
} from '@nestjs/swagger';
import { BaseExceptionDto } from 'src/common/dto/base-exception.dto';

@ApiTags('인증')
@Controller('auth')
export class AuthV0Controller {
  constructor(private authV0Service: AuthV0Service) {}

  @ApiOperation({
    summary: '로그인/회원가입',
    description: '로그인/회원가입',
    deprecated: true,
  })
  @ApiResponse({
    status: HttpStatus.UNAUTHORIZED,
    description: '유효하지 않은 토큰',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @ApiResponse({
    status: HttpStatus.INTERNAL_SERVER_ERROR,
    description: '로그인 서버 에러',
    schema: { $ref: getSchemaPath(BaseExceptionDto) },
  })
  @Post('/')
  async loginUser(@Body() authTokenDTO: AuthV0TokenDto) {
    return this.authV0Service.loginUser(authTokenDTO);
  }
}
