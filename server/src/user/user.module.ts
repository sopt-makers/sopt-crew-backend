import { Module } from '@nestjs/common';
import { UserV0Module } from './v0/user-v0.module';
import { UserV1Module } from './v1/user-v1.module';

@Module({
  imports: [UserV0Module, UserV1Module],
})
export class UserModule {}
