import { ApiProperty } from '@nestjs/swagger';
import { IsEnum } from 'class-validator';
import { FileExtensionType } from 'src/common/enum/file-extension-type.enum';

export class MeetingV1GetPresignedUrlQueryDto {
  @ApiProperty({
    example: 'jpg',
    description: '파일 확장자',
    required: true,
  })
  @IsEnum(FileExtensionType)
  contentType: FileExtensionType;
}
