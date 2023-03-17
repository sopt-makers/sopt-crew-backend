import { Module } from '@nestjs/common';
import { TypeOrmExModule } from 'src/db/typeorm-ex.module';
import { AuthController } from './auth.controller';
import { AuthService } from './auth.service';
import { PassportModule } from '@nestjs/passport';
import { JwtModule } from '@nestjs/jwt';
import { JwtStrategy } from './jwt.strategy';
import { UserRepository } from 'src/users/users.repository';
import { PlaygroundModule } from 'src/internal-api/playground/playground.module';

@Module({
  imports: [
    TypeOrmExModule.forCustomRepository([UserRepository]),
    PassportModule.register({ defaultStrategy: 'jwt' }),
    JwtModule.register({
      secret: 'lee',
      signOptions: {
        expiresIn: 6000 * 6000,
      },
    }),
    PlaygroundModule,
  ],
  controllers: [AuthController],
  providers: [AuthService, JwtStrategy],
})
export class AuthModule {}
