import { Injectable } from '@nestjs/common';
import { User } from 'src/entity/user/user.entity';
import { UserV1GetUserOwnProfileResponseDto } from './dto/user-v1-get-user-own-profile/user-v1-get-user-own-profile-response.dto';

@Injectable()
export class UserV1Service {
  getUserOwnProfile(user: User): UserV1GetUserOwnProfileResponseDto {
    return {
      id: user.id,
      name: user.name,
      orgId: user.orgId,
      profileImage: user.profileImage,
    };
  }
}
