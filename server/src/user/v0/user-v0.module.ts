import { Module } from '@nestjs/common';
import { TypeOrmExModule } from 'src/db/typeorm-ex.module';
import { UserRepository } from '../../entity/user/user.repository';
import { UserV0Controller } from './user-v0.controller';
import { UserV0Service } from './user-v0.service';
import { AuthModule } from 'src/auth/auth.module';
import { JwtStrategy } from 'src/auth/v0/jwt.strategy';
import { ApplyRepository } from 'src/entity/apply/apply.repository';
import { MeetingRepository } from 'src/entity/meeting/meeting.repository';

@Module({
  imports: [
    TypeOrmExModule.forCustomRepository([
      UserRepository,
      ApplyRepository,
      MeetingRepository,
    ]),
    AuthModule,
  ],
  controllers: [UserV0Controller],
  providers: [UserV0Service, JwtStrategy],
})
export class UserV0Module {}
