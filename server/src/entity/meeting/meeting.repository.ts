import { User } from 'src/entity/user/user.entity';
import { CustomRepository } from 'src/db/typeorm-ex.decorator';
import { Brackets, DeleteResult, Repository, UpdateResult } from 'typeorm';
import { MeetingV0GetAllMeetingsQueryDto } from '../../meeting/v0/dto/get-all-meetings/meeting-v0-get-all-meetings-query.dto';
import { MeetingJoinablePart } from './enum/meeting-joinable-part.enum';
import { ACTIVE_GENERATION } from 'src/common/constant/active-generation.const';
import dayjs from 'dayjs';
import { Meeting } from './meeting.entity';
import { MeetingCategory } from './enum/meeting-category.enum';
import { MeetingV0MeetingStatus } from '../../meeting/v0/enum/meeting-v0-meeting-status.enum';
import { ImageURL } from './interface/image-url.interface';

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

  async getMeetingAndCount(user: User): Promise<[Meeting[], number]> {
    const { id } = user;
    const result = await this.createQueryBuilder('meeting')
      .leftJoinAndSelect(
        'meeting.appliedInfo',
        'appliedInfo',
        'appliedInfo.status = :status',
        { status: 1 },
      )
      .leftJoinAndSelect('meeting.user', 'user')
      .where('meeting.userId = :id', { id })
      .getManyAndCount();

    return result;
  }

  // id와 카테고리, 상태로 모임 정보 및 개수 조회
  async getMeetingsAndCount(
    getMeetingDto: MeetingV0GetAllMeetingsQueryDto,
    categoryArr: MeetingCategory[],
    statusArr: MeetingV0MeetingStatus[],
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
      const enumNameInDatabase =
        process.env.NODE_ENV === 'dev'
          ? 'web_dev.meeting_joinableParts_enum[]'
          : 'web.meeting_joinableParts_enum[]';

      /**
       * postgresql에서 배열의 값이 일치하는지 확인하는 방법
       */
      if (Array.isArray(joinableParts)) {
        meetingQuery.andWhere(
          new Brackets((qb) => {
            qb.andWhere(
              `meeting.joinableParts <@ ARRAY[:...joinableParts]::${enumNameInDatabase}`,
              {
                joinableParts,
              },
            );
            qb.andWhere(
              `meeting.joinableParts @> ARRAY[:...joinableParts]::${enumNameInDatabase}`,
              {
                joinableParts,
              },
            );
          }),
        );
      } else {
        meetingQuery.andWhere(
          `meeting.joinableParts @> ARRAY[:joinableParts]::${enumNameInDatabase}`,
          {
            joinableParts,
          },
        );
      }
    }

    meetingQuery.andWhere(
      new Brackets((qb) => {
        statusArr.map((targetStatus, index) => {
          if (targetStatus === MeetingV0MeetingStatus.PRE) {
            index !== 0
              ? qb.orWhere('meeting.startDate > :nowDate', {
                  nowDate,
                }) // 모임 상태가 1개라면 or 조회
              : qb.andWhere('meeting.startDate > :nowDate', {
                  nowDate,
                }); // 모임 상태가 2개 이상이라면 and 조회
          } else if (targetStatus === MeetingV0MeetingStatus.POSSIBLE) {
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
          } else if (targetStatus === MeetingV0MeetingStatus.END) {
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

    meetingQuery.orderBy('meeting.id', 'DESC').skip(skip).take(take);

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
}
