import {
  IsInstance,
  IsNotEmpty,
  IsString,
  ValidateNested,
} from 'class-validator';
import { S3GetPresignedUrlResponseFieldsDto } from './s3-get-presigned-url-response-fields.dto';
import { Type } from 'class-transformer';

/** presigned url 응답 결과 */
export class S3GetPresignedUrlResponseDto {
  /** POST 요청할 url */
  @IsNotEmpty()
  @IsString()
  url: string;

  /** body에 넣을 url */
  @IsInstance(S3GetPresignedUrlResponseFieldsDto)
  @ValidateNested()
  @Type(() => S3GetPresignedUrlResponseFieldsDto)
  fields: S3GetPresignedUrlResponseFieldsDto;
}
