import { User } from 'src/users/user.entity';
import { CustomRepository } from 'src/db/typeorm-ex.decorator';
import { Brackets, DeleteResult, Repository, UpdateResult } from 'typeorm';
import { CreateMeetingDto } from './dto/create-meeting.dto';
import { UpdateMeetingDto } from './dto/update-meeting-dto';
import {
  Meeting,
  ImageURL,
  MeetingStatus,
  MeetingCategory,
} from './meeting.entity';
import { Apply, ApplyStatus, ApplyType } from './apply.entity';
import { ApplyMeetingDto } from './dto/apply-meeting.dto';
import { GetMeetingDto } from './dto/get-meeting.dto';
import { meetingStatus } from 'src/common/utils/meeting.status';
import { PageOptionsDto } from 'src/pagination/dto/page-options.dto';
import { PageMetaDto } from 'src/pagination/dto/page-meta.dto';
import { GetUsersDto } from './dto/get-users.dto';
import axios from 'axios';
import { todayDate } from 'src/common/utils/time';

@CustomRepository(Meeting)
export class MeetingRepository extends Repository<Meeting> {
  async getMeeting(id: number): Promise<Meeting> {
    const result = await this.findOne({
      where: { id },
      relations: ['user', 'appliedInfo'],
    });
    return result;
  }

  async getMeetingsAndCount(
    getMeetingDto: GetMeetingDto,
    categoryArr: MeetingCategory[],
    statusArr: MeetingStatus[],
  ): Promise<[Meeting[], number]> {
    const { query, skip, take } = getMeetingDto;
    const nowDate = todayDate();

    const meetingQuery = await this.createQueryBuilder('meeting')
      .leftJoinAndSelect(
        'meeting.appliedInfo',
        'apply',
        'apply.status = :status',
        { status: 1 },
      )
      .leftJoinAndSelect('meeting.user', 'user')
      .where('1 = 1');

    if (query) {
      meetingQuery.where('meeting.title like :title', { title: `%${query}%` });
    }

    if (categoryArr.length !== 0) {
      meetingQuery.andWhere('meeting.category IN (:...categoryArr)', {
        categoryArr,
      });
    }

    statusArr.map(async (targetStatus, index) => {
      if (targetStatus === MeetingStatus.PRE) {
        const query1 = new Brackets((qb) => {
          qb.where('meeting.startDate > :nowDate', {
            nowDate,
          });
        });
        statusArr.length !== 1 && index !== 0
          ? meetingQuery.orWhere(query1)
          : meetingQuery.andWhere(query1);
      } else if (targetStatus === MeetingStatus.POSSIBLE) {
        const query2 = new Brackets((qb) => {
          qb.where('meeting.startDate <= :nowDate', {
            nowDate,
          }).andWhere('meeting.endDate >= :nowDate', {
            nowDate,
          });
        });
        statusArr.length !== 1 && index !== 0
          ? meetingQuery.orWhere(query2)
          : meetingQuery.andWhere(query2);
      } else if (targetStatus === MeetingStatus.END) {
        const query3 = new Brackets((qb) => {
          qb.where('meeting.endDate < :nowDate', {
            nowDate,
          });
        });
        statusArr.length !== 1 && index !== 0
          ? meetingQuery.orWhere(query3)
          : meetingQuery.andWhere(query3);
      }
    });

    meetingQuery.skip(skip).take(take);

    return await meetingQuery.getManyAndCount();
  }

  async createMeeting(
    createMeetingDto: CreateMeetingDto,
    imageURL: Array<ImageURL>,
    user: User,
  ): Promise<Meeting> {
    const result = await this.create({
      ...createMeetingDto,
      imageURL,
      user,
      appliedInfo: [],
    });
    return result;
  }

  async updateMeetingById(
    id: number,
    updateMeetingDto: UpdateMeetingDto,
    imageURL: Array<ImageURL>,
    user: User,
  ): Promise<UpdateResult> {
    const result = await this.update(
      { id, userId: user.id },
      {
        ...updateMeetingDto,
        imageURL,
      },
    );

    return result;
  }

  async deleteMeeting(id: number): Promise<DeleteResult> {
    const result = await this.delete(id);
    return result;
  }

  async getApply(id: number, type = ApplyType.APPLY): Promise<Apply> {
    const apply = await Apply.findOne({
      where: { id: id, type },
      relations: ['user'],
    });

    return apply;
  }

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
      .andWhere('apply.status IN(:...status)', { status: statusArr });

    applyQuery.skip(skip).take(take);
    return await applyQuery.getManyAndCount();
  }

  async createApply(
    applyMeetingDto: ApplyMeetingDto,
    meeting: Meeting,
    user: User,
    type = ApplyType.APPLY,
  ): Promise<Apply> {
    const { content } = applyMeetingDto;

    // today 고쳐지면 수정해야함
    const appliedDate = todayDate();
    const apply = await Apply.createApply(
      user,
      content,
      meeting,
      type,
      appliedDate,
    );
    return apply;
  }

  async deleteApply(id: number): Promise<DeleteResult> {
    const result = await Apply.delete({ id });
    return result;
  }

  async getInviteUsersByMeeting(id: number, getUsersDto: GetUsersDto) {
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
