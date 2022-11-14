import {
  Body,
  Controller,
  Get,
  Param,
  Post,
  UseGuards,
  ParseIntPipe,
} from '@nestjs/common';
import { AuthService } from './auth.service';
import { AuthTokenDTO } from './dto/auth-token.dto';
import { AuthGuard } from '@nestjs/passport';
import { GetUser } from './get-user.decorator';
import { User } from '../users/user.entity';
import { AuthCredentialsDTO } from './dto/auth-credential.dto';

import {
  ApiTags,
  ApiOperation,
  ApiCreatedResponse,
  ApiSecurity,
  ApiExcludeEndpoint,
  ApiConsumes,
  ApiBody,
  ApiBearerAuth,
  ApiParam,
} from '@nestjs/swagger';

@ApiTags('인증')
@Controller('auth')
export class AuthController {
  constructor(private authService: AuthService) {}

  @ApiOperation({
    summary: '로그인/회원가입',
    description: '로그인/회원가입',
  })
  @Post('/')
  loginUser(@Body() authCredentialsDTO: AuthCredentialsDTO) {
    return this.authService.loginUser(authCredentialsDTO);
  }
}
