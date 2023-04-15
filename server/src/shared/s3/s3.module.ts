import { Module } from '@nestjs/common';
import { S3Repository } from './s3.repository';

@Module({
  providers: [S3Repository],
  exports: [S3Repository],
})
export class S3Module {}
