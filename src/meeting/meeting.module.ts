import { Module } from '@nestjs/common';
import { MeetingController } from './meeting.controller';
import { MeetingService } from './meeting.service';
import { MeetingRepository } from './meeting.repository';
import { TypeOrmExModule } from 'src/db/typeorm-ex.module';
import { S3 } from 'aws-sdk';
import { multerOptionsFactory } from 'src/common/utils/multer.options';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { MulterModule } from '@nestjs/platform-express';
import { AuthModule } from 'src/auth/auth.module';
import { JwtStrategy } from 'src/auth/jwt.strategy';
import { UserRepository } from 'src/users/user.repository';

@Module({
  imports: [
    MulterModule.registerAsync({
      imports: [ConfigModule],
      useFactory: multerOptionsFactory,
      inject: [ConfigService],
    }),
    TypeOrmExModule.forCustomRepository([MeetingRepository, UserRepository]),
    AuthModule,
  ],
  controllers: [MeetingController],
  providers: [MeetingService, JwtStrategy],
})
export class MeetingModule {}
