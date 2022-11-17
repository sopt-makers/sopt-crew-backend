import {
  HttpException,
  HttpStatus,
  UnauthorizedException,
} from '@nestjs/common';
import { User } from 'src/users/user.entity';
import { CustomRepository } from 'src/db/typeorm-ex.decorator';
import { Repository } from 'typeorm';
import { CreateMeetingDto } from './dto/create-meeting.dto';
import { UpdateMeetingDto } from './dto/update-metting-dto';
import { Meeting, ImageURL } from './meeting.entity';
import { Apply } from './apply.entity';
import { ApplyMeetingDto } from './dto/apply-meeting.dto';
import { GetMeetingDto, MeetingStatus } from './dto/get-meeting.dto';
import { GetListDto, ListStatus } from './dto/get-list.dto';
import { UpdateStatusApplyDto } from './dto/update-status-apply.dto';
import { meetingStatus } from 'src/common/utils/meeting.status';

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

    if (status === 1) {
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
    const { date, limit, status } = getListDto;

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

    const apply = await Apply.find({
      where: {
        meetingId: id,
        status:
          status == ListStatus.ALL
            ? null
            : status == ListStatus.APPROVE
            ? 1
            : 2,
      },
      order: { appliedDate: date },
      select: ['appliedDate', 'content', 'id', cUser ? 'status' : 'id'],
      take: limit,
    });

    return apply;
  }

  async getMeetingById(id: number): Promise<Meeting> {
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
    const status = await meetingStatus(meeting);
    meeting.status = status;
    return meeting;
  }

  async getAllMeeting(getMeetingDto: GetMeetingDto) {
    const { category, status, query } = getMeetingDto;
    const nowDate = new Date('2022-03-01');

    const categoryArr = category
      ? category.split(',')
      : ['스터디', '번개', '강연'];

    const moo = await this.createQueryBuilder('meeting')
      .leftJoinAndSelect(
        'meeting.appliedInfo',
        'apply',
        'apply.status = :status',
        { status: 1 },
      )
      .leftJoinAndSelect('meeting.user', 'user');

    if (query) {
      moo.where('meeting.title like :title', { title: `%${query}%` });
    }

    if (categoryArr.length !== 0) {
      moo.andWhere('meeting.category IN (:...categoryArr)', {
        categoryArr,
      });
    }

    let result: Array<any>;

    switch (status) {
      case MeetingStatus.ALL:
        result = await moo.getManyAndCount();
        return { meetings: result[0], count: result[1] };
      case MeetingStatus.BEFORE:
        result = await moo
          .andWhere('meeting.startDate > :nowDate', {
            nowDate,
          })
          .getManyAndCount();
        result[0].forEach(async (item) => {
          const status = await meetingStatus(item);
          item.status = status;
        });
        return { meetings: result[0], count: result[1] };
      case MeetingStatus.OPEN:
        result = await moo
          .andWhere('meeting.startDate <= :nowDate', {
            nowDate,
          })
          .andWhere('meeting.endDate >= :nowDate', {
            nowDate,
          })
          .getManyAndCount();
        result[0].forEach(async (item) => {
          const status = await meetingStatus(item);
          item.status = status;
        });
        const filter1 = result[0].filter((el) => {
          return el.appliedInfo.length < el.capacity ? el : false;
        });
        filter1.forEach(async (item) => {
          const status = await meetingStatus(item);
          item.status = status;
        });
        return { meetings: filter1, count: filter1.length };
      case MeetingStatus.CLOSE:
        result = await moo
          .andWhere('meeting.endDate < :nowDate', {
            nowDate,
          })
          .getManyAndCount();

        const filter2 = result[0].filter((el) => {
          return el.appliedInfo.length >= el.capacity ? el : false;
        });
        filter2.forEach(async (item) => {
          const status = await meetingStatus(item);
          item.status = status;
        });
        return { meetings: filter2, count: filter2.length };
      default:
        console.log('//');
        result = await moo.getManyAndCount();
        result[0].forEach(async (item) => {
          const status = await meetingStatus(item);
          item.status = status;
        });
        return { meetings: result[0], count: result[1] };
    }
  }

  async createMeeting(
    createMeetingDto: CreateMeetingDto,
    files: Array<Express.MulterS3.File>,
    user: User,
  ): Promise<void> {
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
    await this.save(meeting);
    return null;
  }

  async updateMeetingById(
    id: number,
    updateMeetingDto: UpdateMeetingDto,
    files: Array<Express.MulterS3.File>,
  ): Promise<void> {
    const imageURL = files.map((file) => file.location);

    const meeting = await this.update(id, updateMeetingDto);

    return null;
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
      const apply = await Apply.createApply(user, content, meeting);
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
}
