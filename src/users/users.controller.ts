import {
  Controller,
  Get,
  Param,
  ParseIntPipe,
  UseGuards,
} from '@nestjs/common';
import { UsersService } from './users.service';
import {
  ApiTags,
  ApiOperation,
  ApiBearerAuth,
  ApiParam,
} from '@nestjs/swagger';
import { AuthGuard } from '@nestjs/passport';
import { GetUser } from 'src/auth/get-user.decorator';
import { User } from './user.entity';

@ApiTags('사용자')
@Controller('users')
export class UsersController {
  constructor(private usersService: UsersService) {}

  @ApiOperation({
    summary: '내가 만든 모임 조회',
    description: '내가 만든 모임 조회',
  })
  @ApiBearerAuth('access-token')
  @UseGuards(AuthGuard('jwt'))
  @Get('/meeting')
  async getMeetingByUser(@GetUser() user: User) {
    return this.usersService.getMeetingByUser(user);
  }

  @ApiOperation({
    summary: '내가 신청한 모임 조회',
    description: '내가 신청한 모임 조회',
  })
  @ApiBearerAuth('access-token')
  @UseGuards(AuthGuard('jwt'))
  @Get('/apply')
  async getApplyByUser(@GetUser() user: User) {
    return this.usersService.getApplyByUser(user);
  }

  @ApiOperation({
    summary: '유저 상세 조회',
    description: '유저 상세 조회',
  })
  @ApiParam({ name: 'id', required: true, description: '유저 id' })
  @Get('/:id')
  async getUserById(@Param('id', ParseIntPipe) id: number) {
    return this.usersService.getUserById(id);
  }
}
