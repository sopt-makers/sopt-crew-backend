import { HttpException, HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { MeetingRepository } from '../../entity/meeting/meeting.repository';
import { User } from 'src/entity/user/user.entity';
import { ACTIVE_GENERATION } from 'src/common/constant/active-generation.const';
import dayjs from 'dayjs';
import { ImageURL } from '../../entity/meeting/interface/image-url.interface';
import { MeetingV1CreateMeetingBodyDto } from './dto/meeting-v1-create-meeting-body.dto';
import { MeetingV1UpdateMeetingBodyDto } from './dto/meeting-v1-update-meeting-body.dto';
import { S3Repository } from 'src/shared/s3/s3.repository';
import { MeetingV1GetPresignedUrlResponseDto } from './dto/meeting-v1-get-presigned-url-response.dto';

@Injectable()
export class MeetingV1Service {
  constructor(
    @InjectRepository(MeetingRepository)
    private readonly meetingRepository: MeetingRepository,

    private readonly s3Repository: S3Repository,
  ) {}

  /**
   * 미리 서명한 url 생성
   * @author @rdd9223
   * @returns presignedUrl
   */
  async getPresignedUrl(
    contentType: 'jpg' | 'jpeg' | 'png',
  ): Promise<MeetingV1GetPresignedUrlResponseDto> {
    const presignedUrlData = await this.s3Repository.getPresignedUrl(
      'meeting',
      contentType,
    );

    return presignedUrlData;
  }

  /**
   * 미팅 생성
   * @author @rdd9223
   * @param body MeetingV1CreateMeetingBodyDto
   * @param user 유저정보
   * @returns 생성된 미팅 id
   */
  async createMeeting(body: MeetingV1CreateMeetingBodyDto, user: User) {
    if (body.files.length === 0) {
      throw new HttpException(
        { message: '이미지 파일이 없습니다.' },
        HttpStatus.BAD_REQUEST,
      );
    }

    if (body.joinableParts.length === 0) {
      throw new HttpException(
        { message: '한 개 이상의 파트를 입력해주세요' },
        HttpStatus.BAD_REQUEST,
      );
    }

    if (user.activities === null) {
      throw new HttpException(
        { message: '프로필을 입력해주세요' },
        HttpStatus.BAD_REQUEST,
      );
    }

    const { canJoinOnlyActiveGeneration, ...meeting } = body;
    const targetActiveGeneration = this.getTargetActiveGeneration(
      canJoinOnlyActiveGeneration,
    );
    const endDate = dayjs(meeting.endDate).endOf('date').toDate();
    const imageURL = body.files.map((file, index) => ({
      id: index,
      url: file,
    }));

    const result = await this.meetingRepository.createMeeting(
      {
        ...meeting,
        targetActiveGeneration,
        canJoinOnlyActiveGeneration,
        endDate,
      },
      imageURL,
      user,
    );

    return result.id;
  }

  /**
   * 미팅 수정
   * @author @rdd9223
   * @param id meeting id
   * @param body MeetingV1UpdateMeetingBodyDto
   * @param user 유저정보
   * @returns null
   */
  async updateMeetingById(
    id: number,
    body: MeetingV1UpdateMeetingBodyDto,
    user: User,
  ) {
    if (body.joinableParts.length === 0) {
      throw new HttpException(
        { message: '한 개 이상의 파트를 입력해주세요' },
        HttpStatus.BAD_REQUEST,
      );
    }
    if (body.files.length === 0) {
      throw new HttpException(
        { message: '이미지 파일이 없습니다.' },
        HttpStatus.BAD_REQUEST,
      );
    }

    const { canJoinOnlyActiveGeneration, ...meeting } = body;
    const targetActiveGeneration = this.getTargetActiveGeneration(
      canJoinOnlyActiveGeneration,
    );
    const imageURL: Array<ImageURL> = body.files.map((file, index) => ({
      id: index,
      url: file,
    }));
    const endDate = dayjs(meeting.endDate).endOf('date').toDate();

    const result = await this.meetingRepository.updateMeeting(
      id,
      {
        ...meeting,
        targetActiveGeneration,
        canJoinOnlyActiveGeneration,
        endDate,
      },
      imageURL,
      user,
    );

    if (result.affected === 1) {
      return null;
    } else {
      throw new HttpException(
        { message: '조건에 맞는 모임이 없습니다' },
        HttpStatus.BAD_REQUEST,
      );
    }
  }

  /**
   * 현재 활동 기수를 구하는 함수
   * @author @rdd9223
   * @param canJoinOnlyActiveGeneration 생성시 활동 기수만 지원 가능 여부
   * @returns 활동기수만 지원 가능한 경우 현재 활동기수, 아니면 null
   */
  private getTargetActiveGeneration(canJoinOnlyActiveGeneration: boolean) {
    return canJoinOnlyActiveGeneration ? ACTIVE_GENERATION : null;
  }
}
