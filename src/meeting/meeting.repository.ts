import { HttpException, HttpStatus } from '@nestjs/common';
import { User } from 'src/auth/user.entity';
import { UserRepository } from 'src/auth/user.repository';
import { CustomRepository } from 'src/db/typeorm-ex.decorator';
import { LessThan, Like, MoreThan, Repository } from 'typeorm';
import { CreateMeetingDto } from './dto/create-meeting.dto';
import { FilterMeetingDto } from './dto/filter-meeting.dto';
import { UpdateMeetingDto } from './dto/update-metting-dto';
import { Meeting, ImageURL } from './meeting.entity';
import { Apply } from './apply.entity';
import { ApplyMeetingDto } from './dto/apply-meeting.dto';

@CustomRepository(Meeting)
export class MeetingRepository extends Repository<Meeting> {
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
    return meeting;
  }

  async getAllMeeting() {
    const meetings = await this.find({ relations: ['user', 'appliedInfo'] });
    if (!meetings) {
      throw new HttpException(
        { message: '모임이 없습니다' },
        HttpStatus.BAD_REQUEST,
      );
    }
    return meetings;
  }

  async createMeeting(
    createMeetingDto: CreateMeetingDto,
    files: Array<Express.MulterS3.File>,
    user: User,
  ): Promise<void> {
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

  async searchMeeting(filterMeetingDto: FilterMeetingDto): Promise<Meeting[]> {
    const { category, status, query } = filterMeetingDto;
    const nowDate = new Date();

    let statusDate;
    switch (status) {
      case 0:
        statusDate = null;
      case 1:
        statusDate = {
          startDate: LessThan(nowDate),
          endDate: MoreThan(nowDate),
        };
      case 2:
        statusDate = {
          startDate: MoreThan(nowDate),
          endDate: LessThan(nowDate),
        };
      default:
        statusDate = null;
    }

    let querys;
    if (category) {
      querys = category.map((item) => ({
        category: item,
        title: query ? Like(`%${query}%`) : null,
        ...statusDate,
      }));
    } else {
      querys = query ? [{ title: Like(`%${query}%`), ...statusDate }] : null;
    }

    const result = await this.find({
      where: querys,
    });
    return result;
  }

  async searchMeetingByFilter(
    filterMeetingDTO: FilterMeetingDto,
  ): Promise<Meeting[]> {
    const { category, status } = filterMeetingDTO;
    const nowDate = new Date('2022-09-20');

    const statusDate =
      status === 0
        ? null
        : status === 1
        ? {
            startDate: LessThan(nowDate),
            endDate: MoreThan(nowDate),
          }
        : {
            startDate: MoreThan(nowDate),
            endDate: LessThan(nowDate),
          };

    const query = category.map((item) => ({
      category: item,
      ...statusDate,
    }));

    const result = await this.find({
      where: query,
    });

    return result;
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

    const result = meeting.appliedInfo.findIndex(
      (target) => target.user.id === user.id,
    );

    if (result === -1) {
      const apply = await Apply.createApply(user, content, meeting);
      // meeting.appliedUser.push({
      //   content,
      //   appliedDate: nowDate,
      //   user,
      //   status: false,
      // });
      meeting.appliedInfo.push(apply);
    } else {
      const targetApply = meeting.appliedInfo[result];
      meeting.appliedInfo.splice(result, 1);
      await Apply.delete({ id: targetApply.id });
    }
    await this.save(meeting);

    return null;
  }
}
