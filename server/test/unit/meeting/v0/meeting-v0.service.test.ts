import { BadRequestException } from '@nestjs/common';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { APP_INTERCEPTOR } from '@nestjs/core';
import { MulterModule } from '@nestjs/platform-express';
import { Test } from '@nestjs/testing';
import { TypeOrmModule } from '@nestjs/typeorm';
import { AuthModule } from 'src/auth/auth.module';
import { JwtStrategy } from 'src/auth/v0/jwt.strategy';
import { TransformInterceptor } from 'src/common/interceptor/transform.interceptor';
import { multerOptionsFactory } from 'src/common/utils/multer-options.factory';
import { TypeOrmExModule } from 'src/db/typeorm-ex.module';
import { ApplyRepository } from 'src/entity/apply/apply.repository';
import { ApplyStatus } from 'src/entity/apply/enum/apply-status.enum';
import { MeetingRepository } from 'src/entity/meeting/meeting.repository';
import { User } from 'src/entity/user/user.entity';
import { UserRepository } from 'src/entity/user/user.repository';
import { MeetingV0ApplyMeetingDto } from 'src/meeting/v0/dto/meeting-v0-apply-meeting.dto';
import { MeetingV0Controller } from 'src/meeting/v0/meeting-v0.controller';
import { MeetingV0Service } from 'src/meeting/v0/meeting-v0.service';

describe('MeetingV0Service', () => {
  let service: MeetingV0Service;
  let meetingRepository: MeetingRepository;

  beforeAll(async () => {
    const module = await Test.createTestingModule({
      imports: [
        ConfigModule.forRoot({
          isGlobal: true,
          envFilePath:
            process.env.NODE_ENV === 'dev' ? '.dev.env' : '.prod.env',
        }),
        TypeOrmModule.forRootAsync({
          useFactory: (config: ConfigService) => ({
            type: 'postgres',
            host: config.get('DB_HOST'),
            port: parseInt(config.get('DB_PORT')),
            username: config.get('DB_USERNAME'),
            password: config.get('DB_PASSWORD'),
            database: config.get('DB_NAME'),
            entities: [__dirname + '/**/*.entity.{js,ts}'],
            schema: config.get('DB_SCHEMA'),
            synchronize: process.env.NODE_ENV === 'dev' ? true : false,
          }),
          inject: [ConfigService],
        }),
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
      providers: [
        MeetingV0Service,
        JwtStrategy,
        {
          provide: APP_INTERCEPTOR,
          useClass: TransformInterceptor,
        },
      ],
    }).compile();

    service = module.get<MeetingV0Service>(MeetingV0Service);
    meetingRepository = module.get<MeetingRepository>(MeetingRepository);
  });

  describe('applyMeeting', () => {
    it('유저의 활동정보가 빈배열일 때 "기수/파트를 설정해주세요" 예외를 던진다.', async () => {
      const applyMeetingDto = {
        id: 1,
      } as unknown as MeetingV0ApplyMeetingDto;
      const user = {
        id: 2,
        activities: [],
      } as unknown as User;
      jest.spyOn(meetingRepository, 'getMeeting').mockResolvedValue({
        appliedInfo: [{ user: { id: 1 }, status: ApplyStatus.APPROVE }],
      } as any);

      const result = service.applyMeeting(applyMeetingDto, user);

      await expect(result).rejects.toThrowError(
        new BadRequestException('기수/파트를 설정해주세요'),
      );
    });
  });
});
