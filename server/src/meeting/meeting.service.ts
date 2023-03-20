import {
  BadRequestException,
  HttpException,
  HttpStatus,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { MeetingRepository } from './meeting.repository';
import { ImageURL, MeetingCategory, MeetingStatus } from './meeting.entity';
import { CreateMeetingDto } from './dto/create-meeting.dto';
import { UpdateMeetingDto } from './dto/update-meeting-dto';
import { User } from 'src/users/user.entity';
import { ApplyMeetingDto } from './dto/apply-meeting.dto';
import { UserRepository } from 'src/users/users.repository';
import { GetMeetingDto } from './dto/get-meeting.dto';
import { GetListDto } from './dto/get-list.dto';
import { UpdateStatusApplyDto } from './dto/update-status-apply.dto';
import { PageOptionsDto } from 'src/pagination/dto/page-options.dto';
import { InviteMeetingDto } from './dto/invite-meeting.dto';
import { UpdateStatusInviteDto } from './dto/update-status-invite.dto';
import { GetUsersDto } from './dto/get-users.dto';
import { Apply, ApplyStatus, ApplyType } from './apply.entity';
import { PageMetaDto } from 'src/pagination/dto/page-meta.dto';
import { meetingStatus } from 'src/common/utils/meeting.status';
import { Part } from 'src/common/enum/part.enum';
import { MeetingJoinablePart } from './enum/meeting-joinable-part.enum';
import { ACTIVE_GENERATION } from 'src/common/constant/active-generation.const';
import { GetMeetingByIdResponseDto } from './dto/get-meeting-by-id-response.dto';
import { GetAllMeetingsResponseDto } from './dto/get-all-meetings-response.dto';
import { GetApplyListByMeetingResponseDto } from './dto/get-apply-list-by-meeting/get-apply-list-by-meeting-response.dto';

@Injectable()
export class MeetingService {
  constructor(
    @InjectRepository(MeetingRepository)
    private readonly meetingRepository: MeetingRepository,
    private readonly userRepository: UserRepository,
  ) {}

  async updateApplyStatusByMeeting(
    id: number,
    user: User,
    updateStatusApplyDto: UpdateStatusApplyDto,
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

    const apply = await this.meetingRepository.getApply(applyId);

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
    getListDto: GetListDto,
  ): Promise<GetApplyListByMeetingResponseDto> {
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
      await this.meetingRepository.getAppliesAndCount(
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
  ): Promise<GetMeetingByIdResponseDto> {
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

    const { status, approvedApplies } = await meetingStatus(meeting);

    return {
      ...meeting,
      status: status,
      appliedInfo: approvedApplies,
      host: isHost,
      apply: isApply,
      approved: approvedUser,
      invite: inviteUser,
      canJoinOnlyActiveGeneration:
        meeting.targetActiveGeneration === ACTIVE_GENERATION,
    };
  }

  async getAllMeeting(
    getMeetingDto: GetMeetingDto,
  ): Promise<GetAllMeetingsResponseDto> {
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

    const statusArr: MeetingStatus[] = status
      ? status.split(',').map(Number)
      : [];

    const joinablePartArr = Array.isArray(joinableParts)
      ? joinableParts
      : [joinableParts];

    const [meetings, itemCount] =
      await this.meetingRepository.getMeetingsAndCount(
        getMeetingDto,
        categoryArr,
        statusArr,
        joinablePartArr,
        isOnlyActiveGeneration,
      );

    const meetingPromises = meetings.map(async (meeting) => {
      const { status } = await meetingStatus(meeting);

      return {
        ...meeting,
        status,
      };
    });
    await Promise.all(meetingPromises);

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
    createMeetingDto: CreateMeetingDto,
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

    const result = await this.meetingRepository.createMeeting(
      { ...meeting, targetActiveGeneration, canJoinOnlyActiveGeneration },
      imageURL,
      user,
    );

    return result.id;
  }

  async updateMeetingById(
    id: number,
    updateMeetingDto: UpdateMeetingDto,
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

    const result = await this.meetingRepository.updateMeeting(
      id,
      { ...meeting, targetActiveGeneration },
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
  async applyMeeting(applyMeetingDto: ApplyMeetingDto, user: User) {
    const { id } = applyMeetingDto;
    const meeting = await this.meetingRepository.getMeeting(id);

    if (!meeting) {
      throw new HttpException(
        { message: '모임이 없습니다' },
        HttpStatus.BAD_REQUEST,
      );
    }

    const approvedApplies = meeting.appliedInfo.filter(
      (apply) => apply.status === ApplyStatus.APPROVE,
    );

    const applyIndex = meeting.appliedInfo.findIndex(
      (apply) => apply.user.id === user.id,
    );

    if (approvedApplies.length >= meeting.capacity && applyIndex === -1) {
      throw new HttpException(
        { message: '정원이 꽉찼습니다' },
        HttpStatus.BAD_REQUEST,
      );
    }

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
            throw new BadRequestException({ message: '최근 기수가 아닙니다.' });
          }

          return [userActiveGeneration];
        }

        return user.activities;
      })();

      // 유저 파트 검사
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
        throw new BadRequestException({ message: '지원가능 파트가 아닙니다.' });
      }

      await this.meetingRepository.createApply(applyMeetingDto, meeting, user);
      return null;
    }

    // 지원 이력이 있을 경우 지원 삭제
    // 해당 로직은 초대쪽이랑 합쳐서 분리시키면 좋을 듯
    const targetApply = meeting.appliedInfo[applyIndex];
    meeting.appliedInfo.splice(applyIndex, 1);
    await this.meetingRepository.deleteApply(targetApply.id);
    return null;
  }

  async inviteMeeting(inviteMeetingDto: InviteMeetingDto) {
    const { id, message, userIdArr } = inviteMeetingDto;

    const meeting = await this.meetingRepository.getMeeting(id);

    if (!meeting) {
      throw new HttpException(
        { message: '모임이 없습니다' },
        HttpStatus.BAD_REQUEST,
      );
    }

    const users = await this.userRepository.getUsers(userIdArr);

    const invitedApplies = meeting.appliedInfo.filter(
      (item) => item.type === ApplyType.INVITE,
    );

    const invitables = users.filter(
      (user: User) =>
        !invitedApplies.some((apply: Apply) => apply.userId === user.id),
    );

    // 초대 생성 및 초대 배열 return
    const inviteArr = await Promise.all(
      invitables.map((user: User) =>
        this.meetingRepository.createApply(
          { content: message, id },
          meeting,
          user,
          ApplyType.INVITE,
        ),
      ),
    );

    meeting.appliedInfo.push(...inviteArr);
    await meeting.save();
    return null;
  }

  async updateInviteStatusByMeeting(
    id: number,
    user: User,
    updateStatusInviteDto: UpdateStatusInviteDto,
  ): Promise<void> {
    const { applyId, status } = updateStatusInviteDto;

    const apply = await this.meetingRepository.getApply(
      applyId,
      ApplyType.INVITE,
    );

    if (!apply) {
      throw new HttpException(
        { message: '초대 내용이 없습니다' },
        HttpStatus.BAD_REQUEST,
      );
    }

    if (user.id !== apply.user.id) {
      throw new HttpException(
        { message: '초대 승인 권한이 없습니다' },
        HttpStatus.UNAUTHORIZED,
      );
    }

    apply.status = status;
    await apply.save();
    return null;
  }

  async cancelInvite(id: number, inviteId: number, user: User) {
    const meeting = await this.meetingRepository.getMeeting(id);

    if (!meeting) {
      throw new HttpException(
        { message: '모임이 없습니다' },
        HttpStatus.BAD_REQUEST,
      );
    }

    const isHost = meeting.user.id === user.id ? true : false;
    if (!isHost) {
      throw new UnauthorizedException('초대 취소 권한이 없습니다');
    }

    await this.meetingRepository.deleteApply(inviteId);
    return null;
  }

  // 해당 api 수정해야 함
  async getInvitableUsersByMeeting(id: number, getUsersDto: GetUsersDto) {
    return this.meetingRepository.getInvitableUsersByMeeting(id, getUsersDto);
  }

  /**
   * 유저 파트를 미팅 파트로 매핑시켜주는 함수
   * @param userPart 유저 데이터의 파트 정보
   * @returns 유저 파트에 해당하는 미팅 파트
   */
  private mappingPart(userPart: Part): MeetingJoinablePart | null {
    switch (userPart) {
      case Part.ANDROID:
      case Part.ANDROID_LEADER:
        return MeetingJoinablePart.ANDROID;
      case Part.IOS:
      case Part.IOS_LEADER:
        return MeetingJoinablePart.IOS;
      case Part.PM:
      case Part.PM_LEADER:
        return MeetingJoinablePart.PM;
      case Part.SERVER:
      case Part.SERVER_LEADER:
        return MeetingJoinablePart.SERVER;
      case Part.WEB:
      case Part.WEB_LEADER:
        return MeetingJoinablePart.WEB;
      case Part.DESIGN:
      case Part.DESIGN_LEADER:
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
