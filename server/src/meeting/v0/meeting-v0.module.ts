import { Module } from '@nestjs/common';
import { MeetingV0Controller } from './meeting-v0.controller';
import { MeetingV0Service } from './meeting-v0.service';
import { MeetingRepository } from '../../entity/meeting/meeting.repository';
import { TypeOrmExModule } from 'src/db/typeorm-ex.module';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { MulterModule } from '@nestjs/platform-express';
import { AuthModule } from 'src/auth/auth.module';
import { JwtStrategy } from 'src/auth/v0/jwt.strategy';
import { multerOptionsFactory } from 'src/common/utils/multer-options.factory';
import { ApplyRepository } from 'src/entity/apply/apply.repository';
import { UserRepository } from 'src/entity/user/user.repository';

@Module({
  imports: [
    MulterModule.registerAsync({
      imports: [ConfigModule],
      useFactory: multerOptionsFactory,
      inject: [ConfigService],
    }),
    TypeOrmExModule.forCustomRepository([
      MeetingRepository,
      ApplyRepository,
      UserRepository,
    ]),
    AuthModule,
  ],
  controllers: [MeetingV0Controller],
  providers: [MeetingV0Service, JwtStrategy],
})
export class MeetingV0Module {}
