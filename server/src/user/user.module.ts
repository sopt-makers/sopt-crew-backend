import { Module } from '@nestjs/common';
import { UserV0Module } from './v0/user-v0.module';

@Module({
  imports: [UserV0Module],
})
export class UserModule {}
