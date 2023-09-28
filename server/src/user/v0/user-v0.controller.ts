import {
  Controller,
  Get,
  Param,
  ParseIntPipe,
  UseGuards,
} from '@nestjs/common';
import { UserV0Service } from './user-v0.service';
import {
  ApiTags,
  ApiOperation,
  ApiBearerAuth,
  ApiParam,
} from '@nestjs/swagger';
import { AuthGuard } from '@nestjs/passport';
import { User } from '../../entity/user/user.entity';
import { GetUser } from 'src/common/decorator/get-user.decorator';
import { ApiOkResponseCommon } from 'src/common/decorator/api-ok-response-common.decorator';
import { UserV0GetApplyByUserDto } from './dto/get-apply-by-user/user-v0-get-apply-by-user.dto';
import { UserV0GetMeetingByUserDto } from './dto/get-meeting-by-user/user-v0-get-meeting-by-user.dto';

@ApiTags('사용자')
@Controller('users')
export class UserV0Controller {
  constructor(private usersV0Service: UserV0Service) {}

  @ApiOperation({
    summary: '내가 만든 모임 조회',
    description: '내가 만든 모임 조회',
  })
  @ApiOkResponseCommon(UserV0GetMeetingByUserDto)
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Get('/meeting')
  async getMeetingByUser(
    @GetUser() user: User,
  ): Promise<UserV0GetMeetingByUserDto> {
    return this.usersV0Service.getMeetingByUser(user);
  }

  @ApiOperation({
    summary: '내가 신청한 모임 조회',
    description: '내가 신청한 모임 조회',
  })
  @ApiOkResponseCommon(UserV0GetApplyByUserDto)
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Get('/apply')
  async getApplyByUser(
    @GetUser() user: User,
  ): Promise<UserV0GetApplyByUserDto> {
    return this.usersV0Service.getApplyByUser(user);
  }

  @ApiOperation({
    summary: '유저 상세 조회',
    description: '유저 상세 조회',
  })
  @ApiOkResponseCommon(User)
  @ApiParam({ name: 'id', required: true, description: '유저 id' })
  @Get('/:id')
  async getUserById(@Param('id', ParseIntPipe) id: number) {
    return this.usersV0Service.getUserById(id);
  }
}
