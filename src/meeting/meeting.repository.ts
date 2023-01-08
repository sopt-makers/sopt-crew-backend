import {
  HttpException,
  HttpStatus,
  UnauthorizedException,
} from '@nestjs/common';
import { User } from 'src/users/user.entity';
import { CustomRepository } from 'src/db/typeorm-ex.decorator';
import { Brackets, Repository } from 'typeorm';
import { CreateMeetingDto } from './dto/create-meeting.dto';
import { UpdateMeetingDto } from './dto/update-metting-dto';
import { Meeting, ImageURL } from './meeting.entity';
import { Apply, ApplyStatus, ApplyType } from './apply.entity';
import { ApplyMeetingDto } from './dto/apply-meeting.dto';
import { GetMeetingDto } from './dto/get-meeting.dto';
import { GetListDto } from './dto/get-list.dto';
import { UpdateStatusApplyDto } from './dto/update-status-apply.dto';
import { meetingStatus } from 'src/common/utils/meeting.status';
import { PageOptionsDto } from 'src/pagination/dto/page-options.dto';
import { PageMetaDto } from 'src/pagination/dto/page-meta.dto';
import { InviteMeetingDto } from './dto/invite-meeting.dto';
import { UpdateStatusInviteDto } from './dto/update-status-invite.dto';
import { GetUsersDto } from './dto/get-users.dto';
import axios from 'axios';

@CustomRepository(Meeting)
export class MeetingRepository extends Repository<Meeting> {
  async updateApplyStatusByMeeting(
    id: number,
    user: User,
    updateStatusApplyDto: UpdateStatusApplyDto,
  ) {
    const { applyId, status } = updateStatusApplyDto;

    const meeting = await this.findOne({
      where: { id },
      relations: ['user', 'appliedInfo'],
    });

    if (!meeting) {
      throw new HttpException(
        { message: '모임이 없습니다' },
        HttpStatus.BAD_REQUEST,
      );
    }

    const cUser = meeting.user.id === user.id ? true : false;
    if (!cUser) {
      throw new UnauthorizedException('수정 권한이 없습니다');
    }

    const result = await Apply.findOne({ where: { id: applyId } });

    if (!result) {
      throw new HttpException(
        { message: 'id에 맞는 지원자가 없습니다.' },
        HttpStatus.BAD_REQUEST,
      );
    }

    if (status === ApplyStatus.APPROVE) {
      const fliter = meeting.appliedInfo.filter((item) => item.status === 1);
      const result = fliter.findIndex((target) => target.id === user.id);
      if (fliter.length >= meeting.capacity && result == -1) {
        throw new HttpException(
          { message: '정원이 꽉찼습니다' },
          HttpStatus.BAD_REQUEST,
        );
      }
    }

    result.status = status;
    await result.save();
    return null;
  }

  async getListByMeeting(id: number, user: User, getListDto: GetListDto) {
    const { date, status, take, type, skip, page } = getListDto;

    const typeArr = type.split(',');
    const statusArr = status.split(',');

    const meeting = await this.findOne({
      where: { id },
      relations: ['user'],
    });

    if (!meeting) {
      throw new HttpException(
        { message: '모임이 없습니다' },
        HttpStatus.BAD_REQUEST,
      );
    }

    const cUser = meeting.user.id === user.id ? true : false;

    const apply = await Apply.createQueryBuilder('apply')
      .select([
        'apply.id',
        'apply.type',
        'apply.appliedDate',
        'apply.content',
        'apply.status',
        // `${cUser ? 'apply.status' : 'apply.content'}`,
      ])
      .leftJoinAndSelect('apply.user', 'user')
      .where('apply.meetingId = :id', { id })
      .andWhere('apply.type IN(:...type)', { type: typeArr })
      .andWhere('apply.status IN(:...status)', { status: statusArr });

    await apply
      // .orderBy('user.createdAt', 'ASC')
      .skip(skip)
      .take(take);

    const result = await apply.getManyAndCount();

    const pageOptionsDto: PageOptionsDto = { page, skip, take };
    const pageMetaDto = new PageMetaDto({
      itemCount: result[1],
      pageOptionsDto,
    });
    return { meetings: result[0], meta: pageMetaDto };
  }

  async getMeetingById(id: number, user: User): Promise<Meeting> {
    const meeting = await this.createQueryBuilder('meeting')
      .leftJoinAndSelect(
        'meeting.appliedInfo',
        'apply',
        // 'apply.status = :status',
        // { status: 1 },
      )
      .leftJoinAndSelect('meeting.user', 'user')
      .leftJoinAndSelect('apply.user', 'appliedUser')
      .where('meeting.id = :id', { id })
      .getOne();

    if (!meeting) {
      throw new HttpException(
        { message: '모임이 없습니다' },
        HttpStatus.BAD_REQUEST,
      );
    }

    const cUser = meeting.user.id === user.id ? true : false;
    const aUser = meeting.appliedInfo.some(
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

    const { status, confirmedApply } = await meetingStatus(meeting);
    meeting.status = status;
    meeting.appliedInfo = confirmedApply;
    meeting.host = cUser;
    meeting.apply = aUser;
    meeting.approved = approvedUser;
    meeting.invite = inviteUser;
    return meeting;
  }

  async getAllMeeting(getMeetingDto: GetMeetingDto) {
    const { category, status, query, skip, take, page } = getMeetingDto;
    const nowDate = new Date();

    const categoryArr = category
      ? category.split(',')
      : ['스터디', '번개', '강연'];

    const statusArr = status ? status.split(',') : [];

    const moo = await this.createQueryBuilder('meeting')
      .leftJoinAndSelect(
        'meeting.appliedInfo',
        'apply',
        'apply.status = :status',
        { status: 1 },
      )
      .leftJoinAndSelect('meeting.user', 'user')
      .where('1 = 1');

    if (query) {
      moo.where('meeting.title like :title', { title: `%${query}%` });
    }

    if (categoryArr.length !== 0) {
      moo.andWhere('meeting.category IN (:...categoryArr)', {
        categoryArr,
      });
    }

    statusArr.map(async (targetStatus, index) => {
      if (targetStatus === '0') {
        const query1 = new Brackets((qb) => {
          qb.where('meeting.startDate > :nowDate', {
            nowDate,
          });
        });
        statusArr.length !== 1 && index !== 0
          ? moo.orWhere(query1)
          : moo.andWhere(query1);
      } else if (targetStatus === '1') {
        const query2 = new Brackets((qb) => {
          qb.where('meeting.startDate <= :nowDate', {
            nowDate,
          }).andWhere('meeting.endDate >= :nowDate', {
            nowDate,
          });
        });
        statusArr.length !== 1 && index !== 0
          ? moo.orWhere(query2)
          : moo.andWhere(query2);
      } else if (targetStatus === '2') {
        const query3 = new Brackets((qb) => {
          qb.where('meeting.endDate < :nowDate', {
            nowDate,
          });
        });
        statusArr.length !== 1 && index !== 0
          ? moo.orWhere(query3)
          : moo.andWhere(query3);
      }
    });

    await moo
      // .orderBy('user.createdAt', 'ASC')
      .skip(skip)
      .take(take);

    const result = await moo.getManyAndCount();

    result[0].forEach(async (item) => {
      const { status } = await meetingStatus(item);
      item.status = status;
    });

    const pageOptionsDto: PageOptionsDto = { page, skip, take };
    const pageMetaDto = new PageMetaDto({
      itemCount: result[1],
      pageOptionsDto,
    });
    return { meetings: result[0], meta: pageMetaDto };
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

    const imageURL: Array<ImageURL> = files.map((file, index) => ({
      id: index,
      url: file.location,
    }));

    const meeting = await this.create({
      ...createMeetingDto,
      imageURL,
      user,
      appliedInfo: [],
    });
    const result = await this.save(meeting);
    return result.id;
  }

  async updateMeetingById(
    id: number,
    updateMeetingDto: UpdateMeetingDto,
    files: Array<Express.MulterS3.File>,
    user: User,
  ) {
    if (files.length === 0) {
      throw new HttpException(
        { message: '이미지 파일이 없습니다.' },
        HttpStatus.BAD_REQUEST,
      );
    }

    const imageURL: Array<ImageURL> = files.map((file, index) => ({
      id: index,
      url: file.location,
    }));

    const result = await this.update(
      { id, userId: user.id },
      {
        ...updateMeetingDto,
        imageURL,
      },
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
    await this.delete(id);
    return null;
  }

  async applyMeeting(applyMeetingDto: ApplyMeetingDto, user: User) {
    const { id, content } = applyMeetingDto;

    const meeting = await this.findOne({
      where: { id },
      relations: ['user', 'appliedInfo'],
    });

    if (!meeting) {
      throw new HttpException(
        { message: '모임이 없습니다' },
        HttpStatus.BAD_REQUEST,
      );
    }

    const fliter = meeting.appliedInfo.filter((item) => item.status === 1);
    const result = meeting.appliedInfo.findIndex(
      (target) => target.user.id === user.id,
    );

    if (fliter.length >= meeting.capacity && result == -1) {
      throw new HttpException(
        { message: '정원이 꽉찼습니다' },
        HttpStatus.BAD_REQUEST,
      );
    }

    if (result === -1) {
      // 첫 지원
      const type = ApplyType.APPLY;
      const apply = await Apply.createApply(user, content, meeting, type);
      meeting.appliedInfo.push(apply);
    } else {
      // 신청 취소
      console.log(result);
      const targetApply = meeting.appliedInfo[result];
      meeting.appliedInfo.splice(result, 1);
      await Apply.delete({ id: targetApply.id });
    }
    await this.save(meeting);

    return null;
  }

  async inviteMeeting(inviteMeetingDto: InviteMeetingDto) {
    const { id, message, userIdArr } = inviteMeetingDto;

    const users = await User.createQueryBuilder('user')
      .where('user.orgId IN (:...id)', {
        id: userIdArr,
      })
      .getMany();

    const meeting = await this.findOne({
      where: { id },
      relations: ['user', 'appliedInfo'],
    });

    if (!meeting) {
      throw new HttpException(
        { message: '모임이 없습니다' },
        HttpStatus.BAD_REQUEST,
      );
    }

    const filter = meeting.appliedInfo.filter((item) => item.type === 1);
    const result = users.filter(
      (target) => !filter.some((tt) => tt.userId === target.id),
    );

    // if (fliter.length + users.length >= meeting.capacity) {
    //   throw new HttpException(
    //     { message: '정원이 꽉찼습니다' },
    //     HttpStatus.BAD_REQUEST,
    //   );
    // }

    // 첫 지원
    const type = ApplyType.INVITE;
    const inviteArr = await Promise.all(
      result.map((user) => Apply.createApply(user, message, meeting, type)),
    );
    meeting.appliedInfo.push(...inviteArr);
    await this.save(meeting);
  }

  async updateInviteStatusByMeeting(
    id: number,
    user: User,
    updateStatusInviteDto: UpdateStatusInviteDto,
  ) {
    const { applyId, status } = updateStatusInviteDto;

    const meeting = await this.findOne({
      where: { id },
      relations: ['user', 'appliedInfo'],
    });

    const apply = await Apply.findOne({
      where: { id: applyId, type: ApplyType.INVITE },
      relations: ['user'],
    });

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

    // const cUser = meeting.user.id === user.id ? true : false;
    // if (!cUser) {
    //   throw new UnauthorizedException('수정 권한이 없습니다');
    // }

    // const result = await Apply.findOne({ where: { id: applyId } });

    // if (!result) {
    //   throw new HttpException(
    //     { message: 'id에 맞는 지원자가 없습니다.' },
    //     HttpStatus.BAD_REQUEST,
    //   );
    // }

    // if (status === ApplyStatus.APPROVE) {
    //   const fliter = meeting.appliedInfo.filter((item) => item.status === 1);
    //   const result = fliter.findIndex((target) => target.id === user.id);
    //   if (fliter.length >= meeting.capacity && result == -1) {
    //     throw new HttpException(
    //       { message: '정원이 꽉찼습니다' },
    //       HttpStatus.BAD_REQUEST,
    //     );
    //   }
    // }

    // return null;
  }

  async getInviteUsersByMeeting(id: number, getUsersDto: GetUsersDto) {
    const { name, generation } = getUsersDto;

    const invite = await Apply.createQueryBuilder('apply')
      .leftJoinAndSelect('apply.user', 'user')
      .where('apply.type = :type', { type: ApplyType.INVITE })
      .andWhere('apply.meetingId = :meetingId', { meetingId: id })
      .getMany();

    const result = await axios.get<Array<any>>(
      encodeURI(
        name
          ? `https://playground.api.sopt.org/api/v1/members/search?name=${name}`
          : `https://playground.api.sopt.org/api/v1/members/search?name=`,
      ),
    );

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
