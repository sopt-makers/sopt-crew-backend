import { Injectable, InternalServerErrorException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import * as AWS from 'aws-sdk';
import dayjs from 'dayjs';
import { v4 } from 'uuid';
import { S3GetPresignedUrlResponseDto } from './dto/get-presigned-url/s3-get-presigned-url-response.dto';

@Injectable()
export class S3Repository {
  private readonly s3: AWS.S3;

  constructor(private readonly configService: ConfigService) {
    this.s3 = new AWS.S3({
      accessKeyId: configService.get('AWS_ACCESS_KEY_ID'),
      secretAccessKey: configService.get('AWS_SECRET_ACCESS_KEY'),
      region: configService.get('AWS_REGION'),
    });
  }

  /**
   * s3의 presigned url을 생성
   * @param path 파일의 경로명을 랜덤으로 생성
   * @param contentType 파일의 컨텐츠 타입
   * @returns getPresignedUrlResponseDto
   */
  async getPresignedUrl(
    path: 'meeting',
    contentType: string,
  ): Promise<S3GetPresignedUrlResponseDto> {
    try {
      const params = {
        Bucket: this.configService.get('AWS_S3_BUCKET_NAME'),
        Expires: 600, // 1분
        Conditions: [['content-length-range', 0, 10000000]], // 100Byte - 10MB
        Fields: {
          'Content-Type': 'image/jpeg,image/png',
          key: this.getRandomUrl(path, contentType),
        },
      };

      return new Promise(async (resolve, reject) => {
        this.s3.createPresignedPost(params, (err, data) => {
          if (err) {
            reject(err);
          }

          resolve(data as unknown as S3GetPresignedUrlResponseDto);
        });
      });
    } catch (error) {
      throw new InternalServerErrorException(error);
    }
  }

  /**
   * 파일의 경로명을 랜덤으로 생성
   * @author @rdd9223
   * @param path 구분 폴더 명
   * @param contentType 파일의 컨텐츠 타입
   * @returns 파일의 경로명
   */
  private getRandomUrl(path: 'meeting', contentType: string) {
    const uuid = v4();
    const date = dayjs().format('YYYY/MM/DD');

    return `${path}/${date}/${uuid}.${contentType}`;
  }
}
