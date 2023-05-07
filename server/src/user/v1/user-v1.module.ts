import { Module } from '@nestjs/common';
import { UserV1Controller } from './user-v1.controller';
import { UserV1Service } from './user-v1.service';
import { AuthModule } from 'src/auth/auth.module';

@Module({
  imports: [AuthModule],
  controllers: [UserV1Controller],
  providers: [UserV1Service],
})
export class UserV1Module {}
