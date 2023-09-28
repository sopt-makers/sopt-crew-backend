import { Controller, Get, UseGuards } from '@nestjs/common';
import { UserV1Service } from './user-v1.service';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { AuthGuard } from '@nestjs/passport';
import { User } from '../../entity/user/user.entity';
import { GetUser } from 'src/common/decorator/get-user.decorator';
import { UserV1GetUserOwnProfileResponseDto } from './dto/user-v1-get-user-own-profile/user-v1-get-user-own-profile-response.dto';
import { ApiOkResponseCommon } from 'src/common/decorator/api-ok-response-common.decorator';

@ApiTags('사용자')
@Controller('users/v1')
export class UserV1Controller {
  constructor(private usersV1Service: UserV1Service) {}

  @ApiOperation({
    summary: '유저 본인 프로필 조회',
    description: '유저 본인 프로필 조회',
  })
  @ApiOkResponseCommon(UserV1GetUserOwnProfileResponseDto)
  @ApiBearerAuth()
  @UseGuards(AuthGuard('jwt'))
  @Get('/profile/me')
  async getUserOwnProfile(
    @GetUser() user: User,
  ): Promise<UserV1GetUserOwnProfileResponseDto> {
    return this.usersV1Service.getUserOwnProfile(user);
  }
}
