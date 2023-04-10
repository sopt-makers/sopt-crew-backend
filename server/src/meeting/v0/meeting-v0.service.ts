import {
  BadRequestException,
  HttpException,
  HttpStatus,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { MeetingRepository } from '../../entity/meeting/meeting.repository';
import { MeetingV0CreateMeetingDto } from './dto/meeting-v0-create-meeting.dto';
import { MeetingV0UpdateMeetingDto } from './dto/meeting-v0-update-meeting.dto';
import { User } from 'src/entity/user/user.entity';
import { MeetingV0ApplyMeetingDto } from './dto/meeting-v0-apply-meeting.dto';
import { MeetingV0GetMeetingDto } from './dto/meeting-v0-get-meeting.dto';
import { MeetingV0GetListDto } from './dto/meeting-v0-get-list.dto';
import { MeetingV0UpdateStatusApplyDto } from './dto/meeting-v0-update-status-apply.dto';
import { PageOptionsDto } from 'src/common/pagination/dto/page-options.dto';
import { PageMetaDto } from 'src/common/pagination/dto/page-meta.dto';
import { getMeetingStatus } from 'src/common/utils/get-meeting-status.function';
import { UserPart } from 'src/entity/user/enum/user-part.enum';
import { MeetingJoinablePart } from '../../entity/meeting/enum/meeting-joinable-part.enum';
import { ACTIVE_GENERATION } from 'src/common/constant/active-generation.const';
import { MeetingV0GetMeetingByIdResponseDto } from './dto/meeting-v0-get-meeting-by-id-response.dto';
import { MeetingV0GetApplyListByMeetingResponseDto } from './dto/get-apply-list-by-meeting/meeting-v0-get-apply-list-by-meeting-response.dto';
import { MeetingV0GetAllMeetingsResponseDto } from './dto/get-all-meetings/meeting-v0-get-all-meetings-response.dto';
import dayjs from 'dayjs';
import { MeetingCategory } from '../../entity/meeting/meeting.entity';
import { MeetingV0MeetingStatus } from './enum/meeting-v0-meeting-status.enum';
import { ImageURL } from '../../entity/meeting/interface/image-url.interface';
import { ApplyStatus } from 'src/entity/apply/enum/apply-status.enum';
import { ApplyType } from 'src/entity/apply/enum/apply-type.enum';
import { ApplyRepository } from 'src/entity/apply/apply.repository';

@Injectable()
export class MeetingV0Service {
  constructor(
    @InjectRepository(MeetingRepository)
    private readonly meetingRepository: MeetingRepository,

    @InjectRepository(ApplyRepository)
    private readonly applyRepository: ApplyRepository,
  ) {}

  async updateApplyStatusByMeeting(
    id: number,
    user: User,
    updateStatusApplyDto: MeetingV0UpdateStatusApplyDto,
  ) {
    const { applyId, status } = updateStatusApplyDto;

    const meeting = await this.meetingRepository.getMeeting(id);

    if (!meeting) {
      throw new HttpException(
        { message: '모임이 없습니다' },
        HttpStatus.BAD_REQUEST,
      );
    }

    const isHost = meeting.user.id === user.id ? true : false;
    if (!isHost) {
      throw new UnauthorizedException('수정 권한이 없습니다');
    }

    const apply = await this.applyRepository.getApply(applyId);

    if (!apply) {
      throw new HttpException(
        { message: 'applyId에 맞는 지원 이력이 없습니다.' },
        HttpStatus.BAD_REQUEST,
      );
    }

    if (status === ApplyStatus.APPROVE) {
      const approvedApplies = meeting.appliedInfo.filter(
        (apply) => apply.status === ApplyStatus.APPROVE,
      );

      const applyIndex = approvedApplies.findIndex(
        (apply) => apply.id === user.id,
      );

      if (approvedApplies.length >= meeting.capacity && applyIndex == -1) {
        throw new HttpException(
          { message: '정원이 꽉찼습니다' },
          HttpStatus.BAD_REQUEST,
        );
      }
    }

    apply.status = status;
    await apply.save();
    return null;
  }

  async getApplyListByMeeting(
    id: number,
    user: User,
    getListDto: MeetingV0GetListDto,
  ): Promise<MeetingV0GetApplyListByMeetingResponseDto> {
    const { status, take, type, skip, page } = getListDto;

    const typeArr: ApplyType[] = type
      ? type.split(',').map(Number)
      : [ApplyType.APPLY, ApplyType.INVITE];

    const statusArr = status
      ? status.split(',').map(Number)
      : [ApplyStatus.WAITING, ApplyStatus.APPROVE, ApplyStatus.REJECT];

    const meeting = await this.meetingRepository.getMeeting(id);

    if (!meeting) {
      throw new HttpException(
        { message: '모임이 없습니다' },
        HttpStatus.BAD_REQUEST,
      );
    }

    const [applyResponse, itemCount] =
      await this.applyRepository.getAppliesAndCount(
        id,
        typeArr,
        statusArr,
        skip,
        take,
      );

    const pageOptionsDto: PageOptionsDto = { page, skip, take };
    const pageMetaDto = new PageMetaDto({
      itemCount: itemCount,
      pageOptionsDto,
    });

    const applies = (() => {
      // 일반 참여자가 조회 시
      if (meeting.userId !== user.id) {
        return applyResponse.map((apply) => {
          delete apply.content;

          return apply;
        });
      }

      return applyResponse;
    })();

    return { apply: applies, meta: pageMetaDto };
  }

  async getMeetingById(
    id: number,
    user: User,
  ): Promise<MeetingV0GetMeetingByIdResponseDto> {
    const meeting = await this.meetingRepository.getMeeting(id);

    if (!meeting) {
      throw new HttpException(
        { message: '모임이 없습니다' },
        HttpStatus.BAD_REQUEST,
      );
    }

    const isHost = meeting.user.id === user.id ? true : false;

    const isApply = meeting.appliedInfo.some(
      (el) => el.userId === user.id && el.type === ApplyType.APPLY,
    );

    const inviteArr = meeting.appliedInfo.filter((el) => {
      if (el.type === ApplyType.INVITE) {
        return el;
      }
    });

    // 초대되었는지 el.userId === user.i
    // 초대 되었으면 승인을 했는지 el.status === 1
    let approvedUser = false;
    let inviteUser = false;

    inviteArr.forEach((el) => {
      if (el.userId === user.id) {
        inviteUser = true;
        if (el.status === ApplyStatus.APPROVE) {
          approvedUser = true;
        }
      }
    });

    const { status, approvedApplies } = await getMeetingStatus(meeting);

    return {
      ...meeting,
      status: status,
      appliedInfo: meeting.appliedInfo,
      approvedApplyCount: approvedApplies.length,
      host: isHost,
      apply: isApply,
      approved: approvedUser,
      invite: inviteUser,
      canJoinOnlyActiveGeneration:
        meeting.targetActiveGeneration === ACTIVE_GENERATION,
    };
  }

  async getAllMeeting(
    getMeetingDto: MeetingV0GetMeetingDto,
  ): Promise<MeetingV0GetAllMeetingsResponseDto> {
    const {
      category,
      status,
      skip,
      take,
      page,
      isOnlyActiveGeneration,
      joinableParts,
    } = getMeetingDto;

    const categoryArr: MeetingCategory[] = category
      ? (category.split(',') as MeetingCategory[])
      : [
          MeetingCategory.LECTURE,
          MeetingCategory.STUDY,
          MeetingCategory.LIGHTNING,
        ];

    const statusArr: MeetingV0MeetingStatus[] = status
      ? status.split(',').map(Number)
      : [];

    const [meetingResponse, itemCount] =
      await this.meetingRepository.getMeetingsAndCount(
        getMeetingDto,
        categoryArr,
        statusArr,
        isOnlyActiveGeneration,
        joinableParts,
      );

    const meetingPromises = meetingResponse.map(async (meeting) => {
      const { status } = await getMeetingStatus(meeting);

      return {
        ...meeting,
        status,
      };
    });
    const meetings = await Promise.all(meetingPromises);

    const pageOptionsDto: PageOptionsDto = { page, skip, take };
    const pageMetaDto = new PageMetaDto({
      itemCount,
      pageOptionsDto,
    });

    const serializedMeetings = meetings.map((meeting) => {
      const canJoinOnlyActiveGeneration =
        meeting.targetActiveGeneration === ACTIVE_GENERATION &&
        meeting.canJoinOnlyActiveGeneration === true;

      return {
        ...meeting,
        canJoinOnlyActiveGeneration,
      };
    });

    return { meetings: serializedMeetings, meta: pageMetaDto };
  }

  async createMeeting(
    createMeetingDto: MeetingV0CreateMeetingDto,
    files: Array<Express.MulterS3.File>,
    user: User,
  ) {
    if (files.length === 0) {
      throw new HttpException(
        { message: '이미지 파일이 없습니다.' },
        HttpStatus.BAD_REQUEST,
      );
    }

    if (createMeetingDto.joinableParts.length === 0) {
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

    const { canJoinOnlyActiveGeneration, ...meeting } = createMeetingDto;

    const targetActiveGeneration = this.getTargetActiveGeneration(
      canJoinOnlyActiveGeneration,
    );

    const imageURL: Array<ImageURL> = files.map((file, index) => ({
      id: index,
      url: file.location,
    }));

    const endDate = dayjs(meeting.endDate).endOf('date').toDate();

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

  async updateMeetingById(
    id: number,
    updateMeetingDto: MeetingV0UpdateMeetingDto,
    files: Array<Express.MulterS3.File>,
    user: User,
  ) {
    if (updateMeetingDto.joinableParts.length === 0) {
      throw new HttpException(
        { message: '한 개 이상의 파트를 입력해주세요' },
        HttpStatus.BAD_REQUEST,
      );
    }
    if (files.length === 0) {
      throw new HttpException(
        { message: '이미지 파일이 없습니다.' },
        HttpStatus.BAD_REQUEST,
      );
    }

    const { canJoinOnlyActiveGeneration, ...meeting } = updateMeetingDto;

    const targetActiveGeneration = this.getTargetActiveGeneration(
      canJoinOnlyActiveGeneration,
    );

    const imageURL: Array<ImageURL> = files.map((file, index) => ({
      id: index,
      url: file.location,
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

  async deleteMeetingById(id: number): Promise<void> {
    const result = await this.meetingRepository.deleteMeeting(id);

    if (result.affected === 1) {
      return null;
    } else {
      throw new HttpException(
        { message: '조건에 맞는 모임이 없습니다' },
        HttpStatus.BAD_REQUEST,
      );
    }
  }

  /** 미팅 지원/취소 */
  async applyMeeting(applyMeetingDto: MeetingV0ApplyMeetingDto, user: User) {
    const referenceTime = dayjs('2023-03-31');
    const blockedStartTime = referenceTime.startOf('date');
    const blockedEndTime = referenceTime.add(1, 'd').subtract(1, 'hour');
    const now = dayjs();

    if (now.isAfter(blockedStartTime) && now.isBefore(blockedEndTime)) {
      throw new HttpException(
        { message: '32기 스터디는 23:00부터 신청할 수 있어요.' },
        HttpStatus.BAD_REQUEST,
      );
    }

    const { id } = applyMeetingDto;
    const meeting = await this.meetingRepository.getMeeting(id);

    if (!meeting) {
      throw new HttpException(
        { message: '모임이 없습니다' },
        HttpStatus.BAD_REQUEST,
      );
    }

    if (user.activities === null) {
      throw new BadRequestException('기수/파트를 설정해주세요');
    }

    const approvedApplies = meeting.appliedInfo.filter(
      (apply) => apply.status === ApplyStatus.APPROVE,
    );

    const applyIndex = meeting.appliedInfo.findIndex(
      (apply) => apply.user.id === user.id,
    );

    // 지원 이력이 없을 경우 지원 생성
    if (applyIndex === -1) {
      const userActivities = (() => {
        // 현재 기수 제한이 있는 경우
        if (
          meeting.targetActiveGeneration === ACTIVE_GENERATION &&
          meeting.canJoinOnlyActiveGeneration === true
        ) {
          const userActiveGeneration = user.activities.find((activity) => {
            return activity.generation === ACTIVE_GENERATION;
          });

          if (userActiveGeneration === undefined) {
            throw new BadRequestException({ message: '활동 기수가 아닙니다.' });
          }

          return [userActiveGeneration];
        }

        return user.activities;
      })();

      // 파트 제한 있는 경우 유저 파트 검사
      if (meeting.joinableParts !== null && meeting.joinableParts.length > 0) {
        const userJoinableParts = userActivities
          .map((activity) => activity.part)
          .filter((part) => {
            const userRole = this.mappingPart(part);

            // 직책이라면 무조건 패스
            if (userRole === null) {
              return true;
            }

            return meeting.joinableParts.includes(userRole);
          });

        if (userJoinableParts.length === 0) {
          throw new BadRequestException({
            message: '지원가능 파트가 아닙니다.',
          });
        }
      }

      if (approvedApplies.length >= meeting.capacity && applyIndex === -1) {
        throw new HttpException(
          { message: '정원이 꽉찼습니다' },
          HttpStatus.BAD_REQUEST,
        );
      }

      await this.applyRepository.createApply(applyMeetingDto, meeting, user);
      return null;
    }

    // 지원 이력이 있을 경우 지원 삭제
    // 해당 로직은 초대쪽이랑 합쳐서 분리시키면 좋을 듯
    const targetApply = meeting.appliedInfo[applyIndex];
    meeting.appliedInfo.splice(applyIndex, 1);
    await this.applyRepository.deleteApply(targetApply.id);
    return null;
  }

  /**
   * 유저 파트를 미팅 파트로 매핑시켜주는 함수
   * @param userPart 유저 데이터의 파트 정보
   * @returns 유저 파트에 해당하는 미팅 파트
   */
  private mappingPart(userPart: UserPart): MeetingJoinablePart | null {
    switch (userPart) {
      case UserPart.ANDROID:
      case UserPart.ANDROID_LEADER:
        return MeetingJoinablePart.ANDROID;
      case UserPart.IOS:
      case UserPart.IOS_LEADER:
        return MeetingJoinablePart.IOS;
      case UserPart.PM:
      case UserPart.PM_LEADER:
        return MeetingJoinablePart.PM;
      case UserPart.SERVER:
      case UserPart.SERVER_LEADER:
        return MeetingJoinablePart.SERVER;
      case UserPart.WEB:
      case UserPart.WEB_LEADER:
        return MeetingJoinablePart.WEB;
      case UserPart.DESIGN:
      case UserPart.DESIGN_LEADER:
        return MeetingJoinablePart.DESIGN;
      default:
        return null;
    }
  }

  /**
   * 현재 활동 기수를 구하는 함수
   * @param canJoinOnlyActiveGeneration 생성시 활동 기수만 지원 가능 여부
   * @returns 활동기수만 지원 가능한 경우 현재 활동기수, 아니면 null
   */
  private getTargetActiveGeneration(canJoinOnlyActiveGeneration: boolean) {
    return canJoinOnlyActiveGeneration ? ACTIVE_GENERATION : null;
  }
}
