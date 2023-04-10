import { Module } from '@nestjs/common';
import { TypeOrmExModule } from 'src/db/typeorm-ex.module';
import { AuthV0Controller } from './auth-v0.controller';
import { AuthV0Service } from './auth-v0.service';
import { PassportModule } from '@nestjs/passport';
import { JwtModule } from '@nestjs/jwt';
import { JwtStrategy } from './jwt.strategy';
import { PlaygroundModule } from 'src/internal-api/playground/playground.module';
import { UserRepository } from 'src/entity/user/user.repository';
import { ConfigModule, ConfigService } from '@nestjs/config';

@Module({
  imports: [
    TypeOrmExModule.forCustomRepository([UserRepository]),
    PassportModule.register({ defaultStrategy: 'jwt' }),
    JwtModule.registerAsync({
      useFactory: (config: ConfigService) => ({
        secret: config.get('JWT_SECRET'),
        signOptions: {
          expiresIn: 6000 * 6000,
        },
      }),
      inject: [ConfigService],
    }),
    ConfigModule,
    PlaygroundModule,
  ],
  controllers: [AuthV0Controller],
  providers: [AuthV0Service, JwtStrategy],
})
export class AuthV0Module {}
