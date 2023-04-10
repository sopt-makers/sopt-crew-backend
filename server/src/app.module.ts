import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { MeetingModule } from './meeting/meeting.module';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { APP_INTERCEPTOR } from '@nestjs/core';
import { AuthModule } from './auth/auth.module';
import { HealthModule } from './health/health.module';
import { UserModule } from './user/user.module';
import { TransformInterceptor } from './common/interceptor/transform.interceptor';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      envFilePath: process.env.NODE_ENV === 'dev' ? '.dev.env' : '.prod.env',
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
    MeetingModule,
    AuthModule,
    UserModule,
    HealthModule,
  ],
  providers: [
    {
      provide: APP_INTERCEPTOR,
      useClass: TransformInterceptor,
    },
  ],
})
export class AppModule {}
