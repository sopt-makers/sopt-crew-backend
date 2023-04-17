import {
  BadRequestException,
  HttpException,
  HttpStatus,
  Injectable,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { MeetingRepository } from '../../entity/meeting/meeting.repository';
import { User } from 'src/entity/user/user.entity';
import { ACTIVE_GENERATION } from 'src/common/constant/active-generation.const';
import dayjs from 'dayjs';
import { ImageURL } from '../../entity/meeting/interface/image-url.interface';
import { MeetingV1UpdateMeetingBodyDto } from './dto/meeting-v1-update-meeting-body.dto';
import { S3Repository } from 'src/shared/s3/s3.repository';
import { MeetingV1GetPresignedUrlResponseDto } from './dto/get-presigned-url/meeting-v1-get-presigned-url-response.dto';
import { FileExtensionType } from 'src/common/enum/file-extension-type.enum';
import { MeetingV1CreateMeetingResponseDto } from './dto/create-meeting/meeting-v1-create-meeting-response.dto';
import { MeetingV1CreateMeetingBodyDto } from './dto/create-meeting/meeting-v1-create-meeting-body.dto';
import { MeetingV1GetApplyListByMeetingCsvFileUrlQueryDto } from './dto/get-apply-list-by-meeting-csv-file-url/meeting-v1-get-apply-list-by-meeting-csv-file-url-query.dto';
import { ApplyRepository } from 'src/entity/apply/apply.repository';
import * as json2csv from 'json2csv';
import { MeetingV1GetApplyListByMeetingCsvFileUrlResponseDto } from './dto/get-apply-list-by-meeting-csv-file-url/meeting-v1-get-apply-list-by-meeting-csv-file-url-response.dto';

@Injectable()
export class MeetingV1Service {
  constructor(
    @InjectRepository(MeetingRepository)
    private readonly meetingRepository: MeetingRepository,

    @InjectRepository(ApplyRepository)
    private readonly applyRepository: ApplyRepository,

    private readonly s3Repository: S3Repository,
  ) {}

  /**
   * 모임 지원자 목록 csv 파일 url을 가져옴
   * @param id meeting id
   * @param user 유저 정보
   * @param query query 값
   * @returns csv url 정보
   */
  async getApplyListByMeetingCsvFileUrl(
    id: number,
    user: User,
    query: MeetingV1GetApplyListByMeetingCsvFileUrlQueryDto,
  ): Promise<MeetingV1GetApplyListByMeetingCsvFileUrlResponseDto> {
    const meeting = await this.meetingRepository.getMeeting(id);

    if (!meeting) {
      throw new BadRequestException('모임이 없습니다.');
    }

    if (meeting.user.id !== user.id) {
      throw new BadRequestException('권한이 없습니다.');
    }

    const applyList = await this.applyRepository.getAppliesAndCount(
      id,
      query.type,
      query.status,
      0,
    );
    const csvData = applyList[0].map((apply) => {
      const { user, appliedDate } = apply;
      const { name, activities, phone } = user;
      const recentActiveGeneration = activities
        ?.map((activity) => activity.generation)
        .sort((a, b) => b - a)[0];
      const formattedAppliedDate = dayjs(appliedDate).format(
        'YYYY-MM-DD HH:mm:ss',
      );

      return {
        name,
        recentActiveGeneration,
        phone,
        appliedDate: formattedAppliedDate,
      };
    });
    const csvColumns = [
      { value: 'name', label: '이름' },
      { value: 'recentActiveGeneration', label: '활동기수' },
      { value: 'phone', label: '전화번호' },
      { value: 'appliedDate', label: '신청시간' },
    ];
    const csv = json2csv.parse(csvData, { fields: csvColumns });
    const csvBuffer = Buffer.from(csv, 'utf-8');
    const csvUrl = await this.s3Repository.uploadFile({
      path: 'meeting',
      buffer: csvBuffer,
      contentType: FileExtensionType.CSV,
    });

    return { url: csvUrl };
  }

  /**
   * 미리 서명한 url 생성
   * @author @rdd9223
   * @returns presignedUrl
   */
  async getPresignedUrl(
    contentType: FileExtensionType,
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
  async createMeeting(
    body: MeetingV1CreateMeetingBodyDto,
    user: User,
  ): Promise<MeetingV1CreateMeetingResponseDto> {
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

    return { meetingId: result.id };
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

    const { canJoinOnlyActiveGeneration, files, ...meeting } = body;
    const targetActiveGeneration = this.getTargetActiveGeneration(
      canJoinOnlyActiveGeneration,
    );
    const imageURL: Array<ImageURL> = files.map((file, index) => ({
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
