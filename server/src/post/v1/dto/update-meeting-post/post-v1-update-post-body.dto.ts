import { ApiProperty } from '@nestjs/swagger';
import { IsString, IsNotEmpty } from 'class-validator';

/**
 * 게시물 수정 body Dto
 * @author @rdd9223
 */
export class PostV1UpdatePostBodyDto {
  @ApiProperty({
    example: '알고보면 쓸데있는 개발 프로세스',
    description: '모임 제목',
    required: true,
  })
  @IsNotEmpty()
  @IsString()
  title: string;

  @ApiProperty({
    example:
      'https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2023/04/12/7bd87736-b557-4b26-a0d5-9b09f1f1d7df',
    description: '게시글 이미지 리스트',
    required: true,
    isArray: true,
  })
  @IsNotEmpty()
  @IsString({ each: true })
  images: string[];

  @ApiProperty({
    example: 'api 가 터졌다고? 깃이 터졌다고?',
    description: '게시글 내용',
    required: true,
  })
  @IsNotEmpty()
  @IsString()
  contents: string;
}
