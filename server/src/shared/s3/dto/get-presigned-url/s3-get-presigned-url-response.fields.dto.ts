import { IsNotEmpty, IsString } from 'class-validator';

/**
 * get presigned url 응답결과의 body값에 들어갈 필드
 */
export class S3GetPresignedUrlResponseFieldsDto {
  @IsNotEmpty()
  @IsString()
  'Content-Type': string;

  @IsNotEmpty()
  @IsString()
  key: string;

  @IsNotEmpty()
  @IsString()
  bucket: string;

  @IsNotEmpty()
  @IsString()
  'X-Amz-Algorithm': string;

  @IsNotEmpty()
  @IsString()
  'X-Amz-Credential': string;

  @IsNotEmpty()
  @IsString()
  'X-Amz-Date': string;

  @IsNotEmpty()
  @IsString()
  Policy: string;

  @IsNotEmpty()
  @IsString()
  'X-Amz-Signature': string;
}
