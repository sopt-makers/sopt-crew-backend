import { Module } from '@nestjs/common';
import { AuthV0Module } from './v0/auth-v0.module';

@Module({
  imports: [AuthV0Module],
})
export class AuthModule {}
