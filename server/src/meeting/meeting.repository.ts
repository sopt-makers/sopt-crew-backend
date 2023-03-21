import { User } from 'src/users/user.entity';
import { CustomRepository } from 'src/db/typeorm-ex.decorator';
import { Brackets, DeleteResult, Repository, UpdateResult } from 'typeorm';
import {
  Meeting,
  ImageURL,
  MeetingStatus,
  MeetingCategory,
} from './meeting.entity';
import { Apply, ApplyStatus, ApplyType } from './apply.entity';
import { ApplyMeetingDto } from './dto/apply-meeting.dto';
import { GetMeetingDto } from './dto/get-meeting.dto';
import { GetUsersResponseDto } from './dto/get-users-response.dto';
import axios from 'axios';
import { MeetingJoinablePart } from './enum/meeting-joinable-part.enum';
import { ACTIVE_GENERATION } from 'src/common/constant/active-generation.const';
import dayjs from 'dayjs';

@CustomRepository(Meeting)
export class MeetingRepository extends Repository<Meeting> {
  // id로 모임 조회
  async getMeeting(id: number): Promise<Meeting> {
    const result = await this.findOne({
      where: { id },
      relations: ['user', 'appliedInfo'],
    });
    return result;
  }

  // id와 카테고리, 상태로 모임 정보 및 개수 조회
  async getMeetingsAndCount(
    getMeetingDto: GetMeetingDto,
    categoryArr: MeetingCategory[],
    statusArr: MeetingStatus[],
    canJoinOnlyActiveGeneration: boolean,
    joinableParts?: MeetingJoinablePart | MeetingJoinablePart[],
  ): Promise<[Meeting[], number]> {
    const { query, skip, take } = getMeetingDto;
    const nowDate = dayjs().toDate();

    const meetingQuery = this.createQueryBuilder('meeting')
      .leftJoinAndSelect(
        'meeting.appliedInfo',
        'apply',
        'apply.status = :status',
        { status: 1 },
      )
      .leftJoinAndSelect('meeting.user', 'user')
      .where('1 = 1');

    if (query) {
      meetingQuery.andWhere('meeting.title like :title', {
        title: `%${query}%`,
      });
    }

    if (categoryArr.length !== 0) {
      meetingQuery.andWhere('meeting.category IN (:...categoryArr)', {
        categoryArr,
      });
    }

    if (canJoinOnlyActiveGeneration === true) {
      meetingQuery.andWhere(
        'meeting.canJoinOnlyActiveGeneration = :canJoinOnlyActiveGeneration',
        { canJoinOnlyActiveGeneration },
      );
      meetingQuery.andWhere(
        'meeting.targetActiveGeneration = :activeGeneration',
        { activeGeneration: ACTIVE_GENERATION },
      );
    }

    if (joinableParts !== undefined) {
      if (Array.isArray(joinableParts)) {
        meetingQuery.andWhere(
          `meeting.joinableParts && ARRAY[:...joinableParts]::web_dev.meeting_joinableParts_enum[]`,
          {
            joinableParts,
          },
        );
      } else {
        meetingQuery.andWhere(
          `meeting.joinableParts && ARRAY[:joinableParts]::web_dev.meeting_joinableParts_enum[]`,
          {
            joinableParts,
          },
        );
      }
    }

    meetingQuery.andWhere(
      new Brackets((qb) => {
        statusArr.map((targetStatus, index) => {
          if (targetStatus === MeetingStatus.PRE) {
            index !== 0
              ? qb.orWhere('meeting.startDate > :nowDate', {
                  nowDate,
                }) // 모임 상태가 1개라면 or 조회
              : qb.andWhere('meeting.startDate > :nowDate', {
                  nowDate,
                }); // 모임 상태가 2개 이상이라면 and 조회
          } else if (targetStatus === MeetingStatus.POSSIBLE) {
            index !== 0
              ? qb
                  .orWhere('meeting.startDate <= :nowDate', {
                    nowDate,
                  })
                  .andWhere('meeting.endDate >= :nowDate', {
                    nowDate,
                  })
              : qb
                  .andWhere('meeting.startDate <= :nowDate', {
                    nowDate,
                  })
                  .andWhere('meeting.endDate >= :nowDate', {
                    nowDate,
                  });
          } else if (targetStatus === MeetingStatus.END) {
            index !== 0
              ? qb.orWhere('meeting.endDate < :nowDate', {
                  nowDate,
                })
              : qb.andWhere('meeting.endDate < :nowDate', {
                  nowDate,
                });
          }
        });
      }),
    );

    meetingQuery.skip(skip).take(take);

    return await meetingQuery.getManyAndCount();
  }

  // 모임 생성
  async createMeeting(
    meeting: Partial<Meeting>,
    imageURL: Array<ImageURL>,
    user: User,
  ): Promise<Meeting> {
    const result = await this.save({
      ...meeting,
      imageURL,
      user,
      appliedInfo: [],
    });
    return result;
  }

  // 모임 업데이트
  async updateMeeting(
    id: number,
    meeting: Partial<Meeting>,
    imageURL: Array<ImageURL>,
    user: User,
  ): Promise<UpdateResult> {
    const result = await this.update(
      { id, userId: user.id },
      {
        ...meeting,
        imageURL,
      },
    );

    return result;
  }

  // 모임 삭제
  async deleteMeeting(id: number): Promise<DeleteResult> {
    const result = await this.delete(id);
    return result;
  }

  // 지원 조회
  async getApply(id: number, type = ApplyType.APPLY): Promise<Apply> {
    const apply = await Apply.findOne({
      where: { id: id, type },
      relations: ['user'],
    });

    return apply;
  }

  // 조건에 맞는 지원들 and 개수 조회
  async getAppliesAndCount(
    meetingId: number,
    typeArr: ApplyType[],
    statusArr: ApplyStatus[],
    skip: number,
    take: number,
  ) {
    const applyQuery = await Apply.createQueryBuilder('apply')
      .select([
        'apply.id',
        'apply.type',
        'apply.appliedDate',
        'apply.content',
        'apply.status',
      ])
      .leftJoinAndSelect('apply.user', 'user')
      .where('apply.meetingId = :id', { id: meetingId })
      .andWhere('apply.type IN(:...type)', { type: typeArr })
      .andWhere('apply.status IN(:...status)', { status: statusArr })
      .orderBy('apply.appliedDate', 'ASC');

    applyQuery.skip(skip).take(take);
    return await applyQuery.getManyAndCount();
  }

  // 지원 생성
  async createApply(
    applyMeetingDto: ApplyMeetingDto,
    meeting: Meeting,
    user: User,
    type = ApplyType.APPLY,
  ): Promise<Apply> {
    const { content } = applyMeetingDto;

    const appliedDate = dayjs().toDate();
    console.log(dayjs());
    console.log(appliedDate);
    console.log(new Date());
    console.log(process.env.TZ);
    const apply = await Apply.createApply(
      user,
      content,
      meeting,
      type,
      appliedDate,
    );
    console.log(apply);
    return apply;
  }

  // 지원 삭제
  async deleteApply(id: number): Promise<DeleteResult> {
    const result = await Apply.delete({ id });
    return result;
  }

  // 해당 api 수정해야 함
  async getInvitableUsersByMeeting(
    id: number,
    getUsersDto: GetUsersResponseDto,
  ) {
    const { name, generation } = getUsersDto;
    const invite = await Apply.createQueryBuilder('apply')
      .leftJoinAndSelect('apply.user', 'user') // user 정보를 받아온 후 join
      .where('apply.type = :type', { type: ApplyType.INVITE })
      .andWhere('apply.meetingId = :meetingId', { meetingId: id })
      .getMany();

    // playground api에서 이름 조회
    // 추후에 검색 api 제작 요청해야함
    const result = await axios.get<Array<any>>(
      encodeURI(
        name
          ? `https://playground.api.sopt.org/api/v1/members/search?name=${name}`
          : `https://playground.api.sopt.org/api/v1/members/search?name=`,
      ),
    );

    // response data 중 초대 이력이 없는 사람만 걸러내기
    const fin = result.data.filter((item) =>
      generation
        ? item.generation === generation &&
          !invite.some((element) => {
            if (element.user.orgId === item.id) {
              return true;
            }
          })
        : item,
    );

    return fin;
  }
}
